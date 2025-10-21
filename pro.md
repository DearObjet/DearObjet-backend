< 메 인 >

회원가입(일반 / 작가'소품샵)

1. 일반회원 가입
    
    ```jsx
    POST /api/auth/register/general
    ```
    
    ```jsx
    {
    	"userId": "string",
    	"password": "string",
    	"phoneNumber": "string",
    	"termsAgreement": {
    		"required": "boolean",
    		"optional1": "boolean",
    		"optional2": "boolean"
    	}
    }
    ```
    
2. 작가'소품샵 가입
    
    ```jsx
    POST /api/auth/register/business
    ```
    
    ```jsx
    {
      "userId": "string",
      "password": "string",
      "phoneNumber": "string",
      "businessName": "string",
      "businessRegistrationNumber": "string",
      "businessAddress": "string",
      "businessType": "string",
      "businessCategory": "string",
      "businessRegistrationFile": "file",
      "interests": "string",
      "termsAgreement": {
        "required": "boolean",
        "optional1": "boolean", 
        "optional2": "boolean",
        "optional3": "boolean"
      }
    }
    ```
    
3. 아이디 중복확인
    
    ```jsx
    GET /api/auth/check-userid?userId={userId}
    ```
    

      

1. 로그인
    
    ```jsx
    POST /api/auth/login
    ```
    
    ```jsx
    {
    	"userId": "string",
    	"password": "string"
    } 
    ```
    

 

1. 메인 케러셀 배너
    
     
    
    ```jsx
    GET /api/main/banners
    ```
    
    ```jsx
    {
      "banners": [
        {
          "id": "number",
          "imageUrl": "string",
          "linkUrl": "string",
          "title": "string"
        }
      ]
    }
    ```
    
2. 이벤트 페이지
    
    ```jsx
    GET /api/main/events
    ```
    

1. 메인 포스트 리스트
    
    ```jsx
    GET /api/main/posts
    ```
    
    ```jsx
    {
      "posts": [
        {
          "id": "number",
          "title": "string",
          "thumbnailUrl": "string"
        }
      ]
    }
    ```
    
2. 축제 리스트
    
    ```jsx
    GET /api/festivals?page={page}&limit={limit}
    ```
    
    ```jsx
    {
      "festivals": [
        {
          "id": "number",
          "title": "string",
          "thumbnailUrl": "string",
          "startDate": "string",
          "endDate": "string"
        }
      ],
      "pagination": {
        "currentPage": "number",
        "totalPages": "number",
        "totalItems": "number"
      }
    }
    ```
    
3. 공지 & 이벤트
    
    ```jsx
    GET /api/notices?category={category}&page={page}&limit={limit}
    ```
    
    ```jsx
    
    ```
    
- 카테고리[char]
- 내용[text]
- 페이지네이션

1. 뉴스레터 신청
- 기획자 문의

1. 푸터
- 기획자 문의

1. 헤더
- 기획자 문의

< 지 도 >

1. 소품샵 상세정보
    
    ```jsx
    GET /api/shops/{shopId}
    ```
    
    ```jsx
    {
      "id": "number",
      "name": "string",
      "phoneNumber": "string",
      "address": "string",
      "businessHours": "string",
      "images": ["string"],
      "stories": [
        {
          "id": "number",
          "title": "string",
          "content": "string",
          "thumbnailUrl": "string",
          "createdAt": "string"
        }
      ],
      "classes": [
        {
          "id": "number",
          "title": "string",
          "description": "string",
          "thumbnailUrl": "string"
        }
      ]
    }
    ```
    

1. 소품샵 즐겨찾기
    
    ```jsx
    POST /api/shops/{shopId}/bookmark
    DELETE /api/shops/{shopId}/bookmark
    ```
    
2. 소품샵 메세지
    
    ```jsx
    
    ```
    
3. 소품샵 공유 (POST)
    
    ```jsx
    
    ```
    
4. 스토리 리스트 (GET)
    
    ```jsx
    
    ```
    
- 스토리 썸네일[image]
- 스토리 제목[char]
- 스토리 내용[char]
- 작성일자[char]

1. 원데이클래스 리스트 (GET)
- 클래스 썸네일[image]
- 클래스 제목[char]
- 클래스 설명[char]

1. 원데이클래스 리스트 상세
    
    ```jsx
    GET /api/classes/{classId}
    ```
    
    ```jsx
    {
      "id": "number",
      "shopName": "string",
      "title": "string",
      "description": "string",
      "thumbnailUrl": "string",
      "schedule": [
        {
          "date": "string",
          "timeSlots": [
            {
              "time": "string",
              "available": "boolean",
              "maxCapacity": "number",
              "currentBookings": "number"
            }
          ]
        }
      ],
      "importantNotes": "string",
      "guidelines": "string"
    }
    ```
    

1. 원데이클래스 예약하기 (POST)
    
    ```jsx
    POST /api/classes/{classId}/bookings
    ```
    
    ```jsx
    {
      "date": "string",
      "time": "string",
      "participantCount": "number",
      "memo": "string"
    }
    ```
    
2. 입점작가 리스트 (GET)
- 작가 이미지[image]
- 작가 이름[char]

1. 정보 (GET)
-디자인 필요

1. 인근놀거리 리스트 (GET)
(지도 작업하면서 작업가능한지 확인필요.)
- 이미지[image]
- 카테고리[char]
- 제목[char]
- 내용[char]
- 날짜 2025.01.01~2025.01.01[char]

< 포스트 게시판>

1. 포스트 리스트 (GET)
- 포스트 썸네일[image]

1. 포스트 상세 (GET)
- 작성자 이미지[image]
- 작성자 아이디[char]
- 작성시간[int]
- 포스트 썸네일[image]
- 포스트 내용[text]

1. 포스트 작성하기 (POST)

< 작가 리스트 >

1. 배너 (GET) (admin에서 수정되도록 함)

1. 작가 리스트 (GET)
(랜덤 리스트)
- 작가 이미지[image]
- 작가 이름[char]
- 작성시간[int]

1. 작가 리스트 호버시 인스타 피드 (GET)
(작업하면서 확인해봐야 함)