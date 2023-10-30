package util

import (
	"fmt"
	"time"
)

// GetMillis return now time( millisecond )
func GetMillis() int64 {
	return time.Now().UnixNano() / int64(time.Millisecond)
}

// GetSeconds return now time( second )
func GetSeconds() int64 {
	return time.Now().UnixNano() / int64(time.Second)
}

// PrintTimeFromSec 한국 기준으로 시간을 출력 ( YYYY-MM-DD HH:mm:ss )
func PrintTimeFromSec(epoch int64) string {
	t, _ := GetLocalTime(epoch*1000, "Korea")
	strTime := fmt.Sprintf("%d-%02d-%02d %02d:%02d:%02d",
		t.Year(), t.Month(), t.Day(), t.Hour(), t.Minute(), t.Second())
	return strTime
}

// PrintTimeFromMilli 한국 기준으로 시간을 출력 ( YYYY-MM-DD HH:mm:ss )
func PrintTimeFromMilli(epoch int64) string {
	t, _ := GetLocalTime(epoch, "Korea")
	strTime := fmt.Sprintf("%d-%02d-%02d %02d:%02d:%02d",
		t.Year(), t.Month(), t.Day(), t.Hour(), t.Minute(), t.Second())
	return strTime
}

var countryTz = map[string]string{
	"Korea": "Asia/Seoul",
}

func msToTime(ms int64) (time.Time, error) {
	return time.Unix(0, ms*int64(time.Millisecond)), nil
}

// GetLocalTime UTC 타임을 변환
func GetLocalTime(epochMilli int64, location string) (*time.Time, error) {
	loc, err := time.LoadLocation(countryTz[location])
	if err != nil {
		return nil, fmt.Errorf("error. failed to get location[ %s ]", err.Error())
	}
	inTime, err := msToTime(epochMilli)
	if err != nil {
		return nil, fmt.Errorf("error. failed to convert epoch millisecond to time[ %s ]", err.Error())
	}
	retTime := inTime.In(loc)
	return &retTime, nil
}
