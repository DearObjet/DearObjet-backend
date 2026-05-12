BEGIN;

WITH artist_seed AS (
    SELECT
        sample_index,
        'sample-artist-' || lpad(sample_index::text, 2, '0') || '@dearobjet.local' AS email,
        'Sample Artist ' || lpad(sample_index::text, 2, '0') AS name,
        '0109001' || lpad(sample_index::text, 4, '0') AS phone_number,
        'sample-artist-' || lpad(sample_index::text, 2, '0') AS social_id,
        'https://placehold.co/300x300/png?text=Artist+' || lpad(sample_index::text, 2, '0') AS profile_url,
        CASE WHEN sample_index % 2 = 0 THEN 'WHOLESALE_RETAIL' ELSE 'SERVICE' END AS business_type,
        CASE (sample_index - 1) % 5
            WHEN 0 THEN 'ONLINE_MARKETPLACE'
            WHEN 1 THEN 'ECOMMERCE_PLATFORM'
            WHEN 2 THEN 'ECOMMERCE_RETAIL'
            WHEN 3 THEN 'GENERAL_RETAIL'
            ELSE 'CRAFT_RETAIL'
        END AS business_category,
        CASE (sample_index - 1) % 10
            WHEN 0 THEN 'STATIONERY_PAPER'
            WHEN 1 THEN 'INTERIOR_DECOR'
            WHEN 2 THEN 'LIVING_GOODS'
            WHEN 3 THEN 'DESK_OFFICE'
            WHEN 4 THEN 'EMOTIONAL_GOODS_GIFT'
            WHEN 5 THEN 'HANDMADE_CRAFT'
            WHEN 6 THEN 'ILLUSTRATION_ART_GOODS'
            WHEN 7 THEN 'CERAMIC'
            WHEN 8 THEN 'FABRIC_TEXTILE'
            ELSE 'ECO_UPCYCLE'
        END AS specialty
    FROM generate_series(1, 30) AS seed(sample_index)
),
upsert_users AS (
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
        'ARTIST',
        profile_url,
        'ACTIVE',
        social_id,
        null,
        now(),
        now()
    FROM artist_seed
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
    RETURNING user_id, email
),
user_rows AS (
    SELECT
        upsert_users.user_id,
        artist_seed.sample_index,
        artist_seed.email,
        artist_seed.name,
        artist_seed.phone_number,
        artist_seed.business_type,
        artist_seed.business_category,
        artist_seed.specialty
    FROM artist_seed
    JOIN upsert_users ON upsert_users.email = artist_seed.email
),
upsert_business_profiles AS (
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
        user_id,
        business_type,
        '100-' || lpad(sample_index::text, 2, '0') || '-' || lpad((10000 + sample_index)::text, 5, '0'),
        'Sample Studio ' || lpad(sample_index::text, 2, '0'),
        name,
        'Seoul Sample Address ' || sample_index,
        'https://example.com/business-license/sample-artist-' || lpad(sample_index::text, 2, '0') || '.pdf',
        business_category,
        specialty,
        true,
        phone_number,
        'Sample Bank',
        '110' || lpad(sample_index::text, 10, '0'),
        name,
        'https://example.com/bankbook/sample-artist-' || lpad(sample_index::text, 2, '0') || '.png',
        false,
        'tax-sample-artist-' || lpad(sample_index::text, 2, '0') || '@dearobjet.local',
        null,
        now(),
        now()
    FROM user_rows
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
    RETURNING id, user_id
),
business_profile_rows AS (
    SELECT
        upsert_business_profiles.id AS business_profile_id,
        upsert_business_profiles.user_id,
        user_rows.sample_index
    FROM upsert_business_profiles
    JOIN user_rows ON user_rows.user_id = upsert_business_profiles.user_id
),
upsert_artists AS (
    INSERT INTO artists (
        user_id,
        business_profile_id,
        instagram_id,
        created_at,
        updated_at
    )
    SELECT
        user_id,
        business_profile_id,
        'sample_artist_' || lpad(sample_index::text, 2, '0'),
        now(),
        now()
    FROM business_profile_rows
    ON CONFLICT (user_id) DO UPDATE SET
        business_profile_id = EXCLUDED.business_profile_id,
        instagram_id = EXCLUDED.instagram_id,
        updated_at = now()
    RETURNING artists_id
)
SELECT count(*) AS inserted_or_updated_artist_count
FROM upsert_artists;

COMMIT;
