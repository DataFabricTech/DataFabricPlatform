package services

import (
	"github.com/mobigen/golang-web-template/models"
)

// SampleRepository sample usecase(repository)
type SampleRepository interface {
	GetAll() (*[]models.Sample, error)
	GetByID(int) (*models.Sample, error)
	Create(*models.Sample) (*models.Sample, error)
	Update(*models.Sample) (*models.Sample, error)
	Delete(int) (*models.Sample, error)
}

// Sample service - repository - interactor for Sample entity.
type Sample struct {
	Repo SampleRepository
}

// New is constructor that creates Sample service
func (Sample) New(repo SampleRepository) *Sample {
	return &Sample{repo}
}

// GetAll returns All of samples.
func (service *Sample) GetAll() (*[]models.Sample, error) {
	return service.Repo.GetAll()
}

// GetByID returns sample whoes that ID mathces.
func (service *Sample) GetByID(id int) (*models.Sample, error) {
	return service.Repo.GetByID(id)
}

// Create create a new sample.
func (service *Sample) Create(sample *models.Sample) (*models.Sample, error) {
	return service.Repo.Create(sample)
}

// Update update a sample.
func (service *Sample) Update(sample *models.Sample) (*models.Sample, error) {
	return service.Repo.Update(sample)
}

// Delete delete sample from id.
func (service *Sample) Delete(id int) (*models.Sample, error) {
	return service.Repo.Delete(id)
}
