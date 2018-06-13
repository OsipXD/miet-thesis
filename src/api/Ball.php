<?php

namespace app\models\orioks;

use app\models\orioks\km\Km;
use yii\helpers\VarDumper;
use yii\log\Logger;

/**
 * This is the model class for table "ball".
 *
 * @property integer $id
 * @property double $ball
 * @property integer $id_km
 * @property integer $id_stud
 *
 * @property Km $km
 * @property Student $student
 * @property BallChange[] $ballChanges
 */
class Ball extends \app\models\OrioksActiveRecord
{
    /**
     * Студент не посетил занятие
     */
    const N = -1;

    /**
     * @inheritdoc
     */
    public static function tableName()
    {
        return 'ball';
    }

    /**
     * @inheritdoc
     */
    public function rules()
    {
        return [
            [['ball'], 'number'],
            [['id_km', 'id_stud'], 'required'],
            [['id_km','id_stud'], 'integer'],
        ];
    }

    /**
     * Созранит дополнительно изменения балла в лог
     * @inheritdoc
     */
    public function afterSave($insert, $changedAttributes) {
        if(array_key_exists('ball',$changedAttributes)){
            $ballChange = new BallChange();
            $ballChange->id_ball=$this->id;
            $ballChange->old_ball=$changedAttributes['ball'];
            $ballChange->new_ball=$this->ball;
            $ballChange->save();
        }
        parent::afterSave($insert, $changedAttributes);
    }

    /**
     * @inheritdoc
     */
    public function attributeLabels()
    {
        return [
            'id' => 'ID',
            'ball' => 'Ball',
            'id_km' => 'Id Km',
            'id_stud' => 'Студент',
        ];
    }

    /**
     * @return \yii\db\ActiveQuery
     */
    public function getKm()
    {
        return $this->hasOne(Km::className(), ['id' => 'id_km']);
    }

    /**
     * @return \yii\db\ActiveQuery
     */
    public function getStudent()
    {
        return $this->hasOne(Student::className(), ['id' => 'id_stud']);
    }
    /**
     * @return \yii\db\ActiveQuery
     */
    public function getBallChanges()
    {
        return $this->hasMany(BallChange::className(), ['id_ball' => 'id']);
    }

    /**
     * Получить корректный балл студента.
     * В Случае если балл <0 возвращает 0
     * @return double
     */
    public function getCorrectBall() {
        if($this->ball<0) return 0;
        return $this->ball;
    }

    /**
     * Заменить значение балла на балл из лога определённую дату.
     * @param $date
     */
    public function loadBallByDate($date){
        /** @var BallChange $lastChange */
        $lastChange = $this->getBallChanges()->andWhere(['<=','datetime',$date." 23:59:00"])->orderBy('datetime desc')->limit(1)->one();
        $this->ball = $lastChange ? $lastChange->new_ball : $this->ball;
    }
}
