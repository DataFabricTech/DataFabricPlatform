package models

import "github.com/datafabric/gateway/protobuf"

type ResSearch struct {
	Pageable *protobuf.Pageable     `json:"pageable"`
	Filters  map[string]interface{} `json:"filters"`
	Contents *SearchContent         `json:"contents"`
}

type SearchContent struct {
	DataCatalogs []*DataCatalog `json:"dataCatalogs"`
	Storages     []*Storage     `json:"storages"`
}

func (res *ResSearch) Convert(input *protobuf.SearchResponse) (*CommonResponse, error) {
	res.Pageable = input.GetPageable()
	res.ConvertSearchContent(input.Contents)
	res.ConvertSearchFilter(input.Filters)

	return &CommonResponse{
		Code:   200,
		ErrMsg: "",
		Data:   res,
	}, nil
}

func (res *ResSearch) ConvertSearchFilter(filters map[string]*protobuf.ListMapStrNumber) {
	res.Filters = make(map[string]interface{})
	for filterKey, filterValue := range filters {
		tmp := make(map[string]int32)
		for _, v := range filterValue.Value {
			tmp[v.Key] = v.Value
		}
		res.Filters[filterKey] = tmp
	}
}

func (res *ResSearch) ConvertSearchContent(contents *protobuf.SearchContent) {
	res.Contents = &SearchContent{}
	for _, v := range contents.DataCatalogs {
		dc := &DataCatalog{}
		dc.Convert(v)
		res.Contents.DataCatalogs = append(res.Contents.DataCatalogs, dc)
	}
	for _, v := range contents.Storages {
		s := &Storage{}
		s.Convert(v)
		res.Contents.Storages = append(res.Contents.Storages, s)
	}
}
