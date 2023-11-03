# Data Fabric Gateway (Golang 1.21)

Go 언어(Echo Framework, gRPC)를 이용한 Rest To gRPC 게이트웨이 

## 개요  
---
내부 서비스들을 가볍고 빠르게 동작할 수 있도록 gRPC 통신을 채용
화면 개발의 편의를 위한 HTTP + JSON 처리 필요
각 서비스 별 처리보다 하나의 서비스(Like Gateway)에서 처리하도록 결정

따라서 이 서비스(Gateway)는 HTTP(JSON) to gRPC(Proto3)와 그 반대 동작을 처리한다. 
