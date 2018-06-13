<?php

namespace app\models\orioks;

use app\models\orioks\dis\DisInfo;
use app\models\orioks\group\Group;
use app\models\orioks\port\PortAuthor;
use app\models\orioks\practice\LinkStudent;
use app\models\orioks\up\Up;
use app\models\questionnaire\Questionnaire;
use Yii;

/**
 * This is the model class for table "student".
 *
 * @property integer $id
 * @property string $login
 * @property string $numst
 * @property integer $id_group
 * @property integer $id_up
 * @property integer $main
 * @property boolean $active
 * @property boolean $academ
 * @property boolean $invalid
 * @property boolean $needToExpulsion
 *
 * @property Group $group
 * @property Up $up
 * @property DisInfo[] $disInfos
 * @property TimeUnvisit[] $timeUnvisits
 * @property StudMessage[] $studMessages
 * @property Sanction[] $sanctions
 * @property Ball $balls
 * @property DebtBall[] $debtBalls
 * @property PortAuthor[] $portAuthors
 * @property Questionnaire $questionnaire
 * @property LinkStudent $linkStudentContract
 */
class Student extends \app\models\OrioksActiveRecord
{
    use \app\components\name_manager\NameMethodTrait;
    /**
     * @var Up УП студента
     */
    protected $_correctUp;
    /**
     * @var boolean состаяние - должник или нет
     */
    protected $_isDebt;
    /**
     * @var boolean состаяние - должник и имеет долг без даты
     */
    protected $_hasDebtWithoutDate;
    /**
     * @inheritdoc
     */
    public static function tableName()
    {
        return 'student';
    }

    /**
     * @inheritdoc
     */
    public function rules()
    {
        return [
            [['login', 'active'], 'required'],
            [['numst'], 'string'],
            [['id_group', 'id_up', 'main', 'needToExpulsion','academ','active','invalid'], 'integer'],
        ];
    }

    /**
     * @inheritdoc
     */
    public function attributeLabels()
    {
        return [
            'id' => 'ID',
            'login' => 'Login',
            'numst' => 'numst',
            'id_group' => 'Id Group',
            'id_up' => 'Id Up',
            'main' => 'Main',
            'active' => 'Active',
            'academ' => 'Academ',
            'needToExpulsion' => 'needToExpulsion'
        ];
    }
    /**
     * @inheritdoc
     */
    public function fields() {
        $field =  parent::fields();
        $field['fullName']=function (){
            return $this->getFullName();
        };
        $field['shortName']=function (){
            return $this->getShortName();
        };
        $field['last_name']=function (){
            return Yii::$app->get('name_provider')->name($this->login)->last_name;
        };
        return $field;
    }

    /**
     * @inheritdoc
     */
    public function afterSave($insert, $changedAttributes) {
        if(isset($changedAttributes['active']) && $changedAttributes['active']==1 && $this->active==0){
            //Если сюда зашли, значит активность была изменена с "активен" на "не активен". Значит надо закрыть все долги студента
            foreach ($this->disInfos as $disInfo){
                if($disInfo->debt==1 && $disInfo->debt_closed==0){
                    $disInfo->debt_closed=1;
                    $disInfo->save();
                }
            }
        }
        if(array_key_exists('id_up',$changedAttributes) && is_null($changedAttributes['id_up']) && !is_null($this->id_up)){
            //Если сюда зашли, значит студента перевели на ИУП. Значит надо закрыть все долги студента в РУПе

            foreach ($this->group->up->dises as $dis){
                if(!is_null($disInfo=$dis->getDisInfoByStudent($this->id))){
                    if($disInfo->debt==1){
                        $disInfo->debt_closed=1;
                        $disInfo->save();
                    }
                }
            }
        }
        parent::afterSave($insert, $changedAttributes); // TODO: Change the autogenerated stub
    }

    /**
     * @return \yii\db\ActiveQuery
     */
    public function getGroup(){
        return $this->hasOne(Group::className(),['id'=>'id_group']);
    }
    /**
     * @return \yii\db\ActiveQuery
     */
    public function getUp(){
        return $this->hasOne(Up::className(),['id'=>'id_up'])->inverseOf('student');
    }
    /**
     * @return \yii\db\ActiveQuery
     */
    public function getDisInfos()
    {
        return $this->hasMany(DisInfo::className(), ['id_stud' => 'id']);
    }
    /**
     * @return \yii\db\ActiveQuery
     */
    public function getTimeUnvisits()
    {
        return $this->hasMany(TimeUnvisit::className(), ['login' => 'login']);
    }
    /**
     * @return \yii\db\ActiveQuery
     */
    public function getStudMessages()
    {
        return $this->hasMany(StudMessage::className(), ['id_stud' => 'id']);
    }
    /**
     * @return \yii\db\ActiveQuery
     */
    public function getSanctions()
    {
        return $this->hasMany(Sanction::className(), ['id_stud' => 'id']);
    }
    /**
     * @return \yii\db\ActiveQuery
     */
    public function getBalls()
    {
        return $this->hasMany(Ball::className(), ['id_stud' => 'id']);
    }
    /**
     * @return \yii\db\ActiveQuery
     */
    public function getDebtBalls()
    {
        return $this->hasMany(DebtBall::className(), ['id_stud' => 'id']);
    }
    /**
     * @return \yii\db\ActiveQuery
     */
    public function getPortAuthors()
    {
        return $this->hasMany(PortAuthor::className(), ['id_stud' => 'id']);
    }
    /**
     * @return \yii\db\ActiveQuery
     */
    public function getQuestionnaire()
    {
        return $this->hasOne(Questionnaire::className(), ['id_stud' => 'id']);
    }
    /**
     * @return \yii\db\ActiveQuery
     */
    public function getLinkStudentContract()
    {
        return $this->hasOne(LinkStudent::className(), ['id_stud' => 'id']);
    }

    /**
     * Получить УП студента, в зависимости от id_up
     * @return Up
     * @throws \Exception
     */
    public function getCorrectUp(){
        if(is_null($this->_correctUp)){
            if($this->isIndividual()){
                $this->_correctUp=Up::findOne($this->id_up);
            }else{
                $this->_correctUp=Up::findOne($this->group->id_up);
            }
            if(is_null($this->_correctUp)) throw new \Exception("Не найден учебный план!");
        }
        return $this->_correctUp;
    }

    /**
     * Является студент индивидуальщзиком
     * @return bool
     */
    public function isIndividual(){
        return !is_null($this->id_up);
    }

    /**
     * Является ли студент должником
     */
    public function isDebt(){
        if(is_null($this->_isDebt)){
            foreach ($this->disInfos as $disInfo){
                if ($disInfo->debt && !$disInfo->debt_closed){
                    $this->_isDebt=true;
                    goto ret;
                }
            }
            $this->_isDebt=false;
        }
        ret:
        return $this->_isDebt;
    }

    public function hasDebtWithoutDate() {
        if(is_null($this->_hasDebtWithoutDate)){
            if(!$this->isDebt()){
                $this->_hasDebtWithoutDate=false;
                goto ret;
            }

            foreach ($this->disInfos as $disInfo){
                if ($disInfo->debt && !$disInfo->debt_closed && !$disInfo->debt_control_date){
                    $this->_hasDebtWithoutDate=true;
                    goto ret;
                }
            }
            $this->_hasDebtWithoutDate=false;
        }
        ret:
        return $this->_hasDebtWithoutDate;
    }

    /**
     * Получить название группы
     * @return string
     */
    public function getFullGroupName(){
        return $this->group->getFullName($this);
    }

    /**
     * Получить логический идентификатор записи студента.
     * Это строка вида <логин>-<логический идентификатор группы> в нижнем регистре
     * @return string
     */
    public function getLogicalId(){
        return $this->login.'-'.$this->group->getLogicalId();
    }
}