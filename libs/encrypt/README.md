# AES 암호화/복호화 도구

이 프로그램은 AES(Advanced Encryption Standard) 암호화 알고리즘을 사용하여 문자열을 암호화하고 복호화하는 Python 기반의 명령줄 도구입니다.

## 주요 기능

- AES-256 암호화 알고리즘 사용
- ECB (Electronic Code Book) 운영 모드
- Base64 인코딩 지원
- PKCS7 패딩 방식 사용
- 명령줄 인터페이스를 통한 쉬운 사용

## 설치 방법

### 1. Python 가상환경 설정

```bash
# 가상환경 생성
python -m venv venv

# 가상환경 활성화
# Windows의 경우:
venv\Scripts\activate
# macOS/Linux의 경우:
source venv/bin/activate

# 필요한 패키지 설치
pip install -r requirements.txt
```

### 2. 의존성 패키지

- Python 3.x
- pycryptodome==3.19.1

## 사용 방법

### 암호화

```bash
python aes_crypto.py -e "암호화할 문자열"
```

### 복호화

```bash
python aes_crypto.py -d "암호화된_문자열"
```

### 도움말 보기

```bash
python aes_crypto.py --help
```

## 프로그램 상세 설명

### 암호화 과정

1. 입력된 문자열을 UTF-8로 인코딩
2. AES 블록 크기에 맞게 PKCS7 패딩 추가
3. AES-256 ECB 모드로 암호화 수행
4. 결과를 Base64로 인코딩하여 출력

### 복호화 과정

1. Base64로 인코딩된 입력을 디코딩
2. AES-256 ECB 모드로 복호화 수행
3. PKCS7 패딩 제거
4. UTF-8로 디코딩하여 원본 문자열 출력

## 주의사항

1. **보안 고려사항**
   - ECB 모드는 보안성이 낮을 수 있으므로, 실제 프로덕션 환경에서는 CBC나 GCM과 같은 더 안전한 모드 사용을 권장합니다.
   - 시크릿 키는 안전하게 관리되어야 합니다.

2. **데이터 처리**
   - 암호화된 데이터는 Base64로 인코딩되어 출력되므로, 원본 데이터보다 길이가 길어질 수 있습니다.
   - 한글을 포함한 모든 문자열이 지원됩니다.

3. **가상환경 관리**
   - 프로젝트 작업 시 반드시 가상환경을 활성화한 상태에서 실행해야 합니다.
   - 작업이 끝난 후에는 `deactivate` 명령어로 가상환경을 비활성화할 수 있습니다.

## 라이선스

이 프로젝트는 아파치2.0 라이선스를 따릅니다.
