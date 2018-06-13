<?php
namespace app\controllers;

use app\controllers\_base\OrioksController;
use app\kernel\Util;
use app\kernel\helper\OrderHelper;
use app\models\orioks\up\UpType;
use app\models\user\User;
use app\models\user\UserMobile;
use yii\helpers\ArrayHelper;
use app\models\orioks\Fac;
use app\models\orioks\group\Group;
use app\models\orioks\Semester;
use app\models\orioks\Student;
use app\models\orioks\up\UpSegment;
use app\models\orioks\Ball;
use app\models\orioks\dis\Dis;
use ReflectionMethod;
use yii\web\Controller;
use yii\web\NotFoundHttpException;
use yii\web\TooManyRequestsHttpException;
use yii\db\ActiveQuery;
use app\models\news\Notification;
use app\models\orioks\dis\DisInfoStatus;
use app\models\test\ResetForm;
use yii\rest\ActiveController;

class ApiController extends Controller
{
    protected $student;

    /**
     * @inheritDoc
     */
    public function behaviors()
    {
        return array_merge(parent::behaviors(), [
            [
                'class' => \yii\filters\ContentNegotiator::className(),
                'formats' => [
                    'application/json' => \yii\web\Response::FORMAT_JSON,
                ]
            ]
        ]);
    }

    public function beforeAction($action)
    {
        \Yii::$app->response->format = \yii\web\Response::FORMAT_JSON;
        if ($action->id != 'index') {
            $this->student = $this->findStudentByToken();
        }

        return true;
    }

    /**
     * @inheritdoc
     */
    public function afterAction($action, $result)
    {
        // Вызываем parent::parent::afterAction()
        $r = new ReflectionMethod(Controller::className(), 'afterAction');

        return $r->invoke($this, $action, $result);
    }

    public function actionIndex()
    {
        $user_agent = \Yii::$app->request->getHeaders()->get('User-Agent');
        $auth = \Yii::$app->request->getHeaders()->get('Authorization');
        $auth = explode(' ', $auth);
        $auth = base64_decode($auth[1]);
        $auth = explode(':', $auth);
        $user = User::findIdentity($auth[0]);
        if (!$user || !$user->validatePassword($auth[1])) {
            return json_encode(['error' => 'Неверный логин или пароль.'], JSON_UNESCAPED_UNICODE);
        }

        $student = $user->getCurrentStud();
        $token = \Yii::$app->getSecurity()->generateRandomString(32);

        $user_mobile = new UserMobile(['id_stud' => $student->id, 'token' => $token, 'user_agent' => $user_agent]);
        $user_mobile->save();

        return json_encode(['token' => $token]);
    }

    public function actionStudent()
    {
        $student = $this->student;
        $op = $student->getCorrectUp()->getParentByType(UpType::OP);
        $actual_semester = Semester::getActual();

        return [
            'full_name' => $student->getFullName(),
            'group' => $student->group->getShortName(),
            'semester' => $actual_semester->id,
            'op' => $op->name,
            'np' => $op->parent->name,
        ];
    }

    public function actionDis()
    {
        $student = $this->student;

        $id_semester = \Yii::$app->request->get('semester');
        if (is_null($id_semester)) {
            $actual_semester = Semester::getActual();
        } else {
            $id_semester = (int)$id_semester;
            $actual_semester = Semester::findOne(['id' => $id_semester]);
            if (is_null($actual_semester)) throw new \Exception("Передан некорректный идентификатор семестра");
        }

        (new ActiveQuery(UpSegment::className()))->findWith(['semester'], $student->getCorrectUp()->upSegments);
        $actual_week = $actual_semester->getWeekByUp($student->getCorrectUp());
        list($dises, $offset_dises) = $this->getDises($student, $actual_semester, $actual_week, $actual_semester);
        $disciplines = [];
        foreach ($dises AS $k => $dis) {
            $disciplines[$k]['id'] = $dis['id'];
            $disciplines[$k]['name'] = $dis['name'];
            $disciplines[$k]['kaf'] = $dis['science']['kaf']['name'];
            $disciplines[$k]['ball'] = $dis['grade']['b'];
            $disciplines[$k]['maxBall'] = $dis['mvb'];
            $disciplines[$k]['formControl'] = $dis['formControl']['name'];
            $disciplines[$k]['dateExam'] = $dis['date_exam'];
            foreach ($dis['preps'] as $prep) {
                $disciplines[$k]['teachers'][] = $prep['name'];
            }
        }

        return array_values($disciplines);
    }


    /**
     * Возвращает итоговый массив со списком дисциплин студента
     * @param Student $student
     * @param Semester $semester
     * @param int $actual_week
     * @param Semester $actual_semester
     * @return array
     */
    protected function getDises($student, $semester, $actual_week, $actual_semester)
    {
        //Получаем дисциплины студента
        /** @var Dis[] $dises */
        $dises = $student->getCorrectUp()->getDises()->andWhereSem($semester->id)->with('disInfos')->all();
        foreach ($dises as $k => $dis) {//отсеиваем невыбранные дисциплины
            if (!$dis->isChosenByStud($student->id)) {
                unset($dises[$k]);
            }
        }
        $student->getCorrectUp()->populateRelation('dises', $dises);
        //-----------------------------

        (new ActiveQuery(Dis::className()))->findWith([ //подгружаем связные с дисциплинами данные
            'disSegments' => function ($query) use ($semester) {
                /** @var ActiveQuery $query *//*@todo проверить многосеместровые дисциплины*/
                $query->innerJoin(
                    'up_segment',
                    'up_segment.id=dis_segment.id_segment AND up_segment.id_semester=:id_semester',
                    [':id_semester' => $semester->id]
                );
            },
            'disSegments.upSegment',
            'disSegments.allKms.type',
            'disSegments.allKms.kmWeeks',
            'disSegments.allKms.kmActives',
            'disSegments.kms.kmWeeks',
            'disSegments.kms.kmActives',
            'disSegments.kms.balls',
            'disSegments.allKms.irs.file',
            'disSegments.allKms.irs.type',
            'disSegments.allKms.balls' => function ($query) use ($student) {
                /** @var ActiveQuery $query */
                $query->andWhere(['id_stud' => $student->id]);
            },
            'science.kaf',
            'formControl',
            'linkTutorDis.disInfos'
        ], $student->getCorrectUp()->dises);

        /* ----- Подгатавливаем массивы представлению ----- */
        $dis_info = [];//Будет хранить информацию о успеваемости по дисциплине
        $kmGrWeek = [];//Будет хранить информацию о недели КМ у группы
        $now_km = [];//Будет хранить информацию о наличии на текущей неделе КМ
        $km_grade = [];//Будет хранить информацию об оценке за КМ
        $is_offset = [];//Будет хранить информацию является ли дисциплина перезачтенной
        $kmIrs = [];//Будет хранить список ИР КМ
        foreach ($student->getCorrectUp()->dises as &$dis) {//соберем дополнительную информацию, которую потом вставим в массив представлению
            $is_offset[$dis->id] = $dis->isOffsetByStud($student->id);
            //Собираем информацию об успеваемости по дисциплине
            $dis_info[$dis->id]['mvb'] = $dis->getCurrentKmSum($student->id_group, $semester, $actual_week); //Информация о текущем максимально возможносм балле студетна
            $dis_info[$dis->id]['grade'] = Util::solveGrade($dis->getCurrentSumToStudent($student, $semester, $actual_week, $dis_info[$dis->id]['mvb']), $dis_info[$dis->id]['mvb']);//Расчет оценки студента
            $dis_info[$dis->id]['grade']['f'] = $dis->getFullSumToStudent($student->id); //Информация об общей сумме баллов студетна
            $dis_info[$dis->id]['preps'] = $dis->getPrepsByStudent($student);
            $dis_info[$dis->id]['date_exam'] = $dis->getExamDateForGroup($student->id_group);

            //Начинаем собирать информацию о КМ
            OrderHelper::disKmByGroup($dis, $student->id_group, 'allKms');//фильтруем КМ по порядку недель
            foreach ($dis->disSegments[0]->allKms as $km) {
                $kmGrWeek[$km->id] = $km->getWeekForGroup($student->id_group);//тут берем неделю для группы и потом её всунем в массив

                //Считаем оценку
                if (!is_null($km->balls[0]->ball) || $km->isActive($student->group->id)) {
                    $km_grade[$km->id] = Util::solveGrade($km->balls[0]->ball, $km->max_ball); //Если есть балл или КМ активно
                } else {
                    $km_grade[$km->id] = ['b' => '-', 'p' => '-', 'o' => 'n']; //Иначе отстутствие балла - не вина студента
                }
                if ($km_grade[$km->id]['b'] == Ball::N) $km_grade[$km->id]['b'] = 'н';

                if ($actual_semester->id == $semester->id) {//Если выбранный семетр - текущий, то ищем есть ли у дисциплины на текущей неделе КМ
                    if ($kmGrWeek[$km->id] == $actual_week) {
                        $now_km[$dis->id] = true;
                    }
                }
                foreach ($km->irs as $_ir) {
                    $ir = [
                        'id_km' => $km['id'],
                        'name' => $_ir->name,
                        'link' => $_ir->file->getUrl(),
                        'type' => $_ir->type->name,
                        'label' => $_ir->getLabelColor(),
                    ];
                    $kmIrs[$km->id][] = $ir;
                }
            }
        }

        //Переводим все в итоговый массив и наполняет расчитанными в предыдущем цикле значениями
        $a_dises = ArrayHelper::toArray($student->getCorrectUp()->dises, [], true, ['disSegments.allKms.type', 'disSegments.allKms.balls', 'science.kaf', 'formControl']);
        $_a_offset_disses = [];
        foreach ($a_dises as $k => &$dis) {
            if ($is_offset[$dis['id']]) {
                $dis['grade'] = DisInfoStatus::getOffsetBall($is_offset[$dis['id']]);
                $_a_offset_disses[] = $dis;
                unset($a_dises[$k]);
            } else {
                $dis['preps'] = $dis_info[$dis['id']]['preps'];
                $dis['mvb'] = $dis_info[$dis['id']]['mvb'];
                $dis['grade'] = $dis_info[$dis['id']]['grade'];
                $dis['now_km'] = $now_km[$dis['id']];
                $dis['date_exam'] = $dis_info[$dis['id']]['date_exam'];

                foreach ($dis['disSegments'][0]['allKms'] as &$km) {
                    $km['week'] = $kmGrWeek[$km['id']]; //Забираем недели групп
                    $km['grade'] = $km_grade[$km['id']]; //Забираем оценку
                    $km['irs'] = $kmIrs[$km['id']]; //Забираем ИР
                }
            }
        }

        return [$a_dises, $_a_offset_disses];
    }

    protected function findStudentByToken()
    {
        $auth = \Yii::$app->request->getHeaders()->get('Authorization');
        //$auth = 'Bearer UgAYzK8CWLqWDX2a2ZS1IRA0-jB6rgse';
        $auth = explode(' ', $auth);
        if ($auth[0] === 'Bearer' && !empty($auth[1])) {
            $model = UserMobile::find()->where(['token' => $auth[1]])->one();
            if ($model == null) {
                return false;
            }
            $model->last_used = date('Y-m-d H:i:s');
            $model->save();
            $student = Student::find()->where(['id' => $model->id_stud, 'main' => 1])->one();
            return $student;
        }

        return false;
    }
}
