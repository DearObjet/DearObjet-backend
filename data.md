1. 사용자 (Users)

```json
{
  "id": "number (PK)",
  "userId": "string (unique)",
  "password": "string (hashed)",
  "userType": "enum (general, shop_owner, artist, admin)",
  "phoneNumber": "string",
  "name": "string", (일반유저 = 이름 / 작가, 샵 = 대표자명)
  "profileImage": "string",
  "isActive": "boolean",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

1. 사업자 정보 (Business Info)

```json
{
  "id": "number (PK)",
  "userId": "number (FK, UNIQUE, NULLABLE)", **수정
  "businessName": "string", (작가, 샵 = 상호명)
  "businessRegistrationNumber": "string",
  "businessAddress": "string",
  "businessType": "string",
  "businessCategory": "string",
  "businessRegistrationFile": "string",
  "interests": "string",
  "bankName": "string",
  "accountNumber": "string",
  "accountHolder": "string",
  "taxInvoiceEmail": "string",
  "hometaxApiKey": "string"
}
```

1. 소품샵 (Shops)

```json
{
  "id": "number (PK)",
  "ownerId": "number (FK)",
  "name": "string",
  "phoneNumber": "string",
  "address": "string",
  "businessHours": "text",
  "images": "json", **삭제
	"instagramId": "string", **추가
  "latitude": "decimal",
  "longitude": "decimal",
  "isActive": "boolean",
  "createdAt": "timestamp"
}
```

1. 작가 (Artists)

```json
{
  "id": "number (PK)",
  "userId": "number (FK)",
  "artistName": "string", **삭제
  "profileImage": "string", **삭제
  "instagramId": "string",
  "description": "text",
  "specialties": "json", **삭제
  "isActive": "boolean"
}
```

1. 상품 (Products)

```json
{
  "id": "number (PK)",
  "artistId": "number (FK)",
  "name": "string",
  "price": "number",
  "stock": "number",
  "description": "text",
  "images": "json", **수정 images -> image
  "category": "string",
  "memo": "text", **추가
  "isActive": "boolean",
  "createdAt": "timestamp"
}
```

1. 클래스 (Classes)

```json
{
  "id": "number (PK)",
  "shopId": "number (FK)",
  "title": "string",
  "description": "text",
  "price": "number",
  "maxParticipants": "number",
  "duration": "number", **삭제
  "images": "json",
  "importantNotes": "text",
  "guidelines": "text",
  "isActive": "boolean",
  "timeSlotDuration": "number", **추가
  "operatingStartTime": "time", **추가
  "operatingEndTime": "time", **추가
  "operatingDays": "json", **추가
  "createdAt": "timestamp", **추가
  "updatedAt": "timestamp" **추가
```

1. 예약 (Reservations)

```json
{
  "id": "number (PK)",
  "classId": "number (FK)",
  "userId": "number (FK)",
  "reservationNumber": "string (unique)",
  "reservationDate": "date",
  "reservationTime": "time", **삭제
  "startTime": "time", **추가
  "endTime": "time", **추가
  "participantCount": "number",
  "status": "enum (pending, confirmed, cancelled)",
  "memo": "text",
  "createdAt": "timestamp"
}
```

1. 계약 (Contracts)

```json
{
  "id": "number (PK)",
  "shopId": "number (FK)",
  "artistId": "number (FK)",
  "contractContent": "text",
  "startDate": "date",
  "endDate": "date",
  "status": "enum (pending, active, expired, terminated)",
  "signedAt": "timestamp",
  "createdAt": "timestamp"
}
```

1. 정산 (Settlements)

```json
{
  "id": "number (PK)",
  "shopId": "number (FK)",
  "artistId": "number (FK)",
  "settlementDate": "date",
  "totalAmount": "number",
  "commissionRate": "decimal",
  "commissionAmount": "number",
  "settlementAmount": "number",
  "status": "enum (pending, completed, delayed)",
  "paymentMethod": "enum (pg, manual)",
  "processedAt": "timestamp",
  "identificationNumber": "string" **추가
  }
```

1. 메시지 (Messages)

```json
{
  "id": "number (PK)",
  "conversationId": "number (FK)",
  "senderId": "number (FK)",
  "content": "text",
  "messageType": "enum (text, image, file)",
  "isRead": "boolean",
  "sentAt": "timestam
}
```

1. 대화방 (Conservations)

```json
{
  "id": "number (PK)",
  "participant1Id": "number (FK)",
  "participant2Id": "number (FK)",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

1. 포스트 (Posts)

```json
{
  "id": "number (PK)",
  "authorId": "number (FK)",
  "title": "string",
  "content": "text",
  "thumbnailUrl": "string",
  "viewCount": "number",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

1. 스토리 (Stories)

```json
{
  "id": "number (PK)",
  "shopId": "number (FK)",
  "title": "string",
  "content": "text",
  "images": "json",
  "isActive": "boolean",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

1. 축제 (Festivals) **보류 (추후 작업시 재작성)

```json
{
  "id": "number (PK)",
  "title": "string",
  "description": "text",
  "thumbnailUrl": "string",
  "startDate": "date",
  "endDate": "date",
  "location": "string",
  "category": "string",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

1. 공지사항 (Notices)

```json
{
  "id": "number (PK)",
  "category": "enum (major, event, general, guide)",
  "title": "string",
  "content": "text",
  "isImportant": "boolean",
  "isActive": "boolean",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

1. 배너 (Banners) **보류 (추후 작업시 재작성)

```json
{
  "id": "number (PK)",
  "title": "string",
  "imageUrl": "string",
  "linkUrl": "string",
  "bannerType": "enum (main, artist)",
  "orderIndex": "number",
  "isActive": "boolean",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

1. 즐겨찾기 (Bookmarks)

```json
{
  "id": "number (PK)",
  "userId": "number (FK)",
  "shopId": "number",
  "createdAt": "timestamp"
}
```

1. 재고 관리 (InventoryItems)

```json
{
  "id": "number (PK)",
  "shopId": "number (FK)",
  "productId": "number (FK)",
  "stockQuantity": "number",
  "commissionRate": "decimal",
  "lastRestockDate": "date",
  "memo": "text",
  "status": "enum (active, discontinued)",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

1. 출고 관리 (Shipments)

```json
{
  "id": "number (PK)",
  "artistId": "number (FK)",
  "shopId": "number (FK)",
  "status": "enum (pending, confirmed, completed)",
  "createdAt": "timestamp",
  "shippedAt": "timestamp",
  "confirmedAt": "timestamp"
}
```

1. 출고 상품 상제 (ShipmentItems)

```json
{
  "id": "number (PK)",
  "shipmentId": "number (FK)",
  "productId": "number (FK)",
  "quantity": "number",
  "createdAt": "timestamp",
  "memo": "text" **보류
}
```

1. 운영시간 (BusinessHours)

```json
{
  "id": "number (PK)",
  "shopId": "number (FK)",
  "dayOfWeek": "enum (monday, tuesday, wednesday, thursday, friday, saturday, sunday)",
  "isOpen": "boolean",
  "openTime": "time",
  "closeTime": "time",
}
```

1. 계약서 템플릿 (ContractTemplates)

```json
{
  "id": "number (PK)",
  "shopId": "number (FK)",
  "title": "string",
  "content": "text",
  "contractorName": "string",
  "contractDate": "date",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

1. 세금계산서 (TaxInvoices) **보류 (추후 작업시 재작성)

```json
{
  "id": "number (PK)",
  "settlementId": "number (FK)",
  "invoiceNumber": "string (unique)",
  "supplierRegistrationNumber": "string",
  "supplierBusinessNumber": "string",
  "supplierBusinessName": "string",
  "supplierRepresentativeName": "string",
  "supplierBusinessType": "string",
  "supplierBusinessCategory": "string",
  "supplierEmail": "string",
  "recipientRegistrationNumber": "string",
  "recipientBusinessNumber": "string",
  "recipientBusinessName": "string",
  "recipientRepresentativeName": "string",
  "recipientBusinessType": "string",
  "recipientBusinessCategory": "string",
  "recipientEmail": "string",
  "issueDate": "date",
  "totalAmount": "number",
  "supplyAmount": "number",
  "taxAmount": "number",
  "items": "json",
  "status": "enum (issued, sent, completed)",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

1. 알림 설정 (NotificationSettings)

```json
{
  "id": "number (PK)",
  "userId": "number (FK)",
  "settlementNotification": "boolean",
  "reservationCancellationNotification": "boolean",
  "newApplicationNotification": "boolean",
  "taxInvoiceRequestNotification": "boolean",
  "contractExpirationNotification": "boolean",
  "kakaoTalkNotification": "boolean",
  "emailNotification": "boolean",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

1. 결제 수단 (PaymentMethods) **보류 (확인필요)

```json
{
  "id": "number (PK)",
  "userId": "number (FK)",
  "methodType": "enum (card, account)",
  "cardNumber": "string",
  "cardType": "string",
  "expiryDate": "string",
  "bankName": "string",
  "accountNumber": "string",
  "businessRegistrationNumber": "string",
  "isActive": "boolean",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

1. 결제 내역 (PaymentHistory) **보류(확인 필요)

```json
{
  "id": "number (PK)",
  "userId": "number (FK)",
  "planName": "string",
  "amount": "number",
  "paymentMethod": "enum (card, account)",
  "status": "enum (completed, failed, cancelled)",
  "paymentDate": "timestamp",
  "createdAt": "timestamp"
}
```

1. 약관 동의 (TermsAgreements)

```json
{
  "id": "number (PK)",
  "userId": "number (FK)",
  "requiredTerms": "boolean",
  "optionalTerms1": "boolean",
  "optionalTerms2": "boolean",
  "optionalTerms3": "boolean",
  "agreedAt": "timestamp"
}
```