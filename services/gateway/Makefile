# Copyright (c) 2021 Mobigen JBLIM. All Rights Reserved.

################################################################################
##                             PROGRAM PARAMS                                 ##
################################################################################

# program name and version info 
TARGET := gateway
VERSION := v1.0.0
IMAGE ?= repo.iris.tools/iris/$(TARGET):$(VERSION)

################################################################################

GO ?= $(shell command -v go 2> /dev/null)
MACHINE = $(shell uname -m)
GOFLAGS ?= $(GOFLAGS:)
BUILD_TIME := $(shell date -u +%Y%m%d.%H%M%S)
BUILD_HASH := $(shell git rev-parse --short HEAD)

################################################################################

MODULE_NAME := $(shell head -1 go.mod | awk '{print $$2}')
LDFLAGS += -X '$(MODULE_NAME)/common/appdata.Name=$(TARGET)'
LDFLAGS += -X '$(MODULE_NAME)/common/appdata.Version=$(VERSION)'
LDFLAGS += -X '$(MODULE_NAME)/common/appdata.BuildHash=$(BUILD_HASH)'

################################################################################
##                             Docker PARAMS                                 ##
################################################################################

## Docker Build Versions
DOCKER_BUILD_IMAGE = golang:1.21.3
DOCKER_BASE_IMAGE = alpine:3.13.2

# Binaries.
TOOLS_BIN_DIR := $(abspath bin)
GO_INSTALL = ./scripts/go_install.sh

MOCKGEN_VER := v1.4.3
MOCKGEN_BIN := mockgen
MOCKGEN := $(TOOLS_BIN_DIR)/$(MOCKGEN_BIN)-$(MOCKGEN_VER)

GOCOV_VER := latest
GOCOV_BIN := gocov
GOCOV_GEN := $(TOOLS_BIN_DIR)/$(GOCOV_BIN)

GOCOV-HTML_VER := latest
GOCOV-HTML_BIN := gocov-html
GOCOV-HTML_GEN := $(TOOLS_BIN_DIR)/$(GOCOV-HTML_BIN)

OUTDATED_VER := master
OUTDATED_BIN := go-mod-outdated
OUTDATED_GEN := $(TOOLS_BIN_DIR)/$(OUTDATED_BIN)

GOLINT_VER := master
GOLINT_BIN := golint
GOLINT_GEN := $(TOOLS_BIN_DIR)/$(GOLINT_BIN)

export GO111MODULE=on

## Checks the code style, tests, builds and bundles.
all: check-style dist

## Runs govet and gofmt against all packages.
.PHONY: check-style
check-style: govet lint
	@echo Checking for style guide compliance

## Runs lint against all packages.
.PHONY: lint
lint: $(GOLINT_GEN)
	@echo Running lint
	$(GOLINT_GEN) -set_exit_status ./...
	@echo lint success

## Runs govet against all packages.
.PHONY: vet
govet:
	@echo Running govet
	$(GO) vet ./...
	@echo Govet success

## Builds and thats all :)
.PHONY: dist
dist:	build

.PHONY: build
build: ## Build binary
	@echo Building $(TARGET)
	GOOS=linux GOARCH=amd64 CGO_ENABLED=1 $(GO) build -ldflags "$(LDFLAGS)" -gcflags all=-trimpath=$(PWD) -asmflags all=-trimpath=$(PWD) \
	     -a -installsuffix cgo -o build/bin/$(TARGET) main.go

build-image:  ## Build the docker image 
	@echo Building $(TARGET) Docker Image
	docker build \
	--build-arg DOCKER_BUILD_IMAGE=$(DOCKER_BUILD_IMAGE) \
	--build-arg DOCKER_BASE_IMAGE=$(DOCKER_BASE_IMAGE) \
	. -f build/Dockerfile -t $(IMAGE) \
	--no-cache

## For Dev Run
HOME = $(shell pwd)
PROFILE = "prod"
.PHONY: run
run: 
	mkdir -p db &&  \
	HOME=$(HOME) PROFILE=$(PROFILE) ./build/bin/$(TARGET)

# Generate mocks from the interfaces.
.PHONY: mocks
mocks:  $(MOCKGEN)
	go generate ./...

.PHONY: verify-mocks
verify-mocks:  $(MOCKGEN) mocks
	@if !(git diff --quiet HEAD); then \
		echo "generated files are out of date, run make mocks"; exit 1; \
	fi

.PHONY: unittest
unittest:
	$(GO) test ./... -v -covermode=count -coverprofile=coverage.out

.PHONY: gocov 
gocov: $(GOCOV_GEN) ## Runs gocov
	$(GOCOV_GEN) test ./... | gocov-html > cov-out.html 

.PHONY: check-modules
check-modules: $(OUTDATED_GEN) ## Check outdated modules
	@echo Checking outdated modules
	$(GO) list -u -m -json all | $(OUTDATED_GEN) -update -direct


## Clean Cache
.PHONY: clean
clean: 
	go clean -i -cache -testcache

## --------------------------------------
## Tooling Binaries
## --------------------------------------

$(MOCKGEN): ## Build mockgen.
	GOBIN=$(TOOLS_BIN_DIR) $(GO_INSTALL) github.com/golang/mock/mockgen $(MOCKGEN_BIN) $(MOCKGEN_VER)

$(OUTDATED_GEN): ## Build go-mod-outdated.
	GOBIN=$(TOOLS_BIN_DIR) $(GO_INSTALL) github.com/psampaz/go-mod-outdated $(OUTDATED_BIN) $(OUTDATED_VER)

$(GOLINT_GEN): ## Build golint.
	GOBIN=$(TOOLS_BIN_DIR) $(GO_INSTALL) golang.org/x/lint/golint $(GOLINT_BIN) $(GOLINT_VER)

## gocov, gocov-html.
$(GOCOV_GEN): 
	GOBIN=$(TOOLS_BIN_DIR) $(GO_INSTALL) github.com/axw/gocov/gocov $(GOCOV_BIN) $(GOCOV_VER) && \
		  GOBIN=$(TOOLS_BIN_DIR) $(GO_INSTALL) github.com/matm/gocov-html $(GOCOV-HTML_BIN) $(GOCOV-HTML_VER)

