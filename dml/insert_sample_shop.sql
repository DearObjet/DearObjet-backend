-- 로컬 개발용 SHOP 유저 1건 시드
-- users -> business_profile -> shops 순으로 FK를 이어서 넣는다.
-- 재실행해도 안전하도록 email / user_id 기준 UPSERT.

BEGIN;

WITH shop_seed AS (
    SELECT
        'sample-shop-01@dearobjet.local'   AS email,
        '샘플 소품샵 사장'                    AS name,
        '01099880001'                      AS phone_number,
        'sample-shop-01'                   AS social_id,
        'https://placehold.co/300x300/png?text=Shop+01' AS profile_url
),
upsert_user AS (
    INSERT INTO users (
        email,
        name,
        phone_number,
        sms_agreement,
        marketing_agreement,
        role,
        profile_url,
        user_status,
        social_id,
        withdrawal_requested_at,
        created_at,
        updated_at
    )
    SELECT
        email,
        name,
        phone_number,
        true,
        true,
        'SHOP',
        profile_url,
        'ACTIVE',
        social_id,
        null,
        now(),
        now()
    FROM shop_seed
    ON CONFLICT (email) DO UPDATE SET
        name = EXCLUDED.name,
        phone_number = EXCLUDED.phone_number,
        sms_agreement = EXCLUDED.sms_agreement,
        marketing_agreement = EXCLUDED.marketing_agreement,
        role = EXCLUDED.role,
        profile_url = EXCLUDED.profile_url,
        user_status = EXCLUDED.user_status,
        social_id = EXCLUDED.social_id,
        withdrawal_requested_at = null,
        updated_at = now()
    RETURNING user_id
),
upsert_business_profile AS (
    INSERT INTO business_profile (
        user_id,
        business_type,
        business_number,
        business_name,
        owner_name,
        business_address,
        business_license_url,
        business_category,
        specialty,
        review_data_agreement,
        business_phone_number,
        bank_name,
        bank_account_number,
        account_holder,
        bankbook_image_url,
        is_bankbook_verified,
        tax_invoice_email,
        hometax_api_key,
        created_at,
        updated_at
    )
    SELECT
        upsert_user.user_id,
        'WHOLESALE_RETAIL',
        '201-88-10001',
        '디어오브제 샘플샵',
        shop_seed.name,
        '서울특별시 성동구 연무장길 33',
        'https://example.com/business-license/sample-shop-01.pdf',
        'CRAFT_RETAIL',
        'HANDMADE_CRAFT',
        true,
        shop_seed.phone_number,
        '국민은행',
        '11009988000101',
        shop_seed.name,
        'https://example.com/bankbook/sample-shop-01.png',
        false,
        'tax-sample-shop-01@dearobjet.local',
        null,
        now(),
        now()
    FROM upsert_user
    CROSS JOIN shop_seed
    ON CONFLICT (user_id) DO UPDATE SET
        business_type = EXCLUDED.business_type,
        business_number = EXCLUDED.business_number,
        business_name = EXCLUDED.business_name,
        owner_name = EXCLUDED.owner_name,
        business_address = EXCLUDED.business_address,
        business_license_url = EXCLUDED.business_license_url,
        business_category = EXCLUDED.business_category,
        specialty = EXCLUDED.specialty,
        review_data_agreement = EXCLUDED.review_data_agreement,
        business_phone_number = EXCLUDED.business_phone_number,
        bank_name = EXCLUDED.bank_name,
        bank_account_number = EXCLUDED.bank_account_number,
        account_holder = EXCLUDED.account_holder,
        bankbook_image_url = EXCLUDED.bankbook_image_url,
        is_bankbook_verified = EXCLUDED.is_bankbook_verified,
        tax_invoice_email = EXCLUDED.tax_invoice_email,
        hometax_api_key = EXCLUDED.hometax_api_key,
        updated_at = now()
    RETURNING id AS business_profile_id, user_id
),
upsert_shop AS (
    INSERT INTO shops (
        user_id,
        business_profile_id,
        latitude,
        longitude,
        instagram_id,
        shop_name,
        shop_description,
        created_at,
        updated_at
    )
    SELECT
        user_id,
        business_profile_id,
        37.5445,   -- 성수동 좌표. 지도 API 안 타고 바로 박음
        127.0557,
        'sample_shop_01',
        '디어오브제 샘플샵',
        '로컬 테스트용 샘플 소품샵입니다.',
        now(),
        now()
    FROM upsert_business_profile
    ON CONFLICT (user_id) DO UPDATE SET
        business_profile_id = EXCLUDED.business_profile_id,
        latitude = EXCLUDED.latitude,
        longitude = EXCLUDED.longitude,
        instagram_id = EXCLUDED.instagram_id,
        shop_name = EXCLUDED.shop_name,
        shop_description = EXCLUDED.shop_description,
        updated_at = now()
    RETURNING shop_id, user_id, business_profile_id
)
SELECT shop_id, user_id, business_profile_id
FROM upsert_shop;

COMMIT;
