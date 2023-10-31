package models

// Sample ....
type Sample struct {
	ID       int    `json:"id" gorm:"column:id;primaryKey;autoIncrement"`
	Name     string `json:"name" gorm:"column:name;not null"`
	Desc     string `json:"desc" gorm:"column:desc;size:256"`
	CreateAt int64  `json:"createAt" gorm:"column:createAt;autoCreateTime:milli"`
}
