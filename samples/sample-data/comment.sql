
COMMENT ON TABLE actor IS '出演者';

COMMENT ON COLUMN actor.actor_id IS '出演者ID';
COMMENT ON COLUMN actor.first_name IS '名';
COMMENT ON COLUMN actor.last_name IS '姓';
COMMENT ON COLUMN actor.last_update IS '最終更新日';

COMMENT ON TABLE address IS '住所';

COMMENT ON COLUMN address.address_id IS '住所ID';
COMMENT ON COLUMN address.address IS '住所1';
COMMENT ON COLUMN address.address2 IS '住所2';
COMMENT ON COLUMN address.district IS '地区';
COMMENT ON COLUMN address.city_id IS '都市ID';
COMMENT ON COLUMN address.postal_code IS '郵便番号';
COMMENT ON COLUMN address.phone IS '電話番号';
COMMENT ON COLUMN address.last_update IS '最終更新日';

COMMENT ON TABLE category IS 'カテゴリー';

COMMENT ON COLUMN category.category_id IS 'カテゴリーID';
COMMENT ON COLUMN category.name IS 'カテゴリー名';
COMMENT ON COLUMN category.last_update IS '最終更新日';

COMMENT ON TABLE city IS '都市';

COMMENT ON COLUMN city.city_id IS '都市ID';
COMMENT ON COLUMN city.city IS '都市名';
COMMENT ON COLUMN city.country_id IS '国ID';
COMMENT ON COLUMN city.last_update IS '最終更新日';

COMMENT ON TABLE country IS '国';

COMMENT ON COLUMN country.country_id IS '国ID';
COMMENT ON COLUMN country.country IS '国名';
COMMENT ON COLUMN country.last_update IS '最終更新日';

COMMENT ON TABLE customer IS '顧客';

COMMENT ON COLUMN customer.customer_id IS '顧客ID';
COMMENT ON COLUMN customer.store_id IS '店舗ID';
COMMENT ON COLUMN customer.first_name IS '名';
COMMENT ON COLUMN customer.last_name IS '姓';
COMMENT ON COLUMN customer.email IS 'メール';
COMMENT ON COLUMN customer.address_id IS '住所ID';
COMMENT ON COLUMN customer.activebool IS '有効フラグ';
COMMENT ON COLUMN customer.create_date IS '作成日';
COMMENT ON COLUMN customer.last_update IS '最終更新日';
COMMENT ON COLUMN customer.active IS '有効区分';

COMMENT ON TABLE film IS 'フィルム';

COMMENT ON COLUMN film.film_id IS 'フィルムID';
COMMENT ON COLUMN film.title IS 'タイトル';
COMMENT ON COLUMN film.description IS '説明';
COMMENT ON COLUMN film.release_year IS '発売日';
COMMENT ON COLUMN film.language_id IS '言語ID';
COMMENT ON COLUMN film.rental_duration IS 'レンタル期間';
COMMENT ON COLUMN film.rental_rate IS 'レンタル料';
COMMENT ON COLUMN film.length IS '時間';
COMMENT ON COLUMN film.replacement_cost IS '交換費用';
COMMENT ON COLUMN film.rating IS '評価';
COMMENT ON COLUMN film.last_update IS '最終更新日';
COMMENT ON COLUMN film.special_features IS '特記事項';
COMMENT ON COLUMN film.fulltext IS '全文';

COMMENT ON TABLE film_actor IS '出演者';

COMMENT ON COLUMN film_actor.actor_id IS '出演者ID';
COMMENT ON COLUMN film_actor.film_id IS 'フィルムID';
COMMENT ON COLUMN film_actor.last_update IS '最終更新日';

COMMENT ON TABLE film_category IS 'フィルムカテゴリー';

COMMENT ON COLUMN film_category.film_id IS 'フィルムID';
COMMENT ON COLUMN film_category.category_id IS 'カテゴリーID';
COMMENT ON COLUMN film_category.last_update IS '最終更新日';

COMMENT ON TABLE inventory IS '在庫';

COMMENT ON COLUMN inventory.inventory_id IS '在庫ID';
COMMENT ON COLUMN inventory.film_id IS 'フィルムID';
COMMENT ON COLUMN inventory.store_id IS '店舗ID';
COMMENT ON COLUMN inventory.last_update IS '最終更新日';

COMMENT ON TABLE language IS '言語';

COMMENT ON COLUMN language.language_id IS '言語ID';
COMMENT ON COLUMN language.name IS '言語名';
COMMENT ON COLUMN language.last_update IS '最終更新日';

COMMENT ON TABLE payment IS '支払';

COMMENT ON COLUMN payment.payment_id IS '支払ID';
COMMENT ON COLUMN payment.customer_id IS '顧客ID';
COMMENT ON COLUMN payment.staff_id IS 'スタッフID';
COMMENT ON COLUMN payment.rental_id IS 'レンタルID';
COMMENT ON COLUMN payment.amount IS '料金';
COMMENT ON COLUMN payment.payment_date IS '支払日';

COMMENT ON TABLE rental IS 'レンタル';

COMMENT ON COLUMN rental.rental_id IS 'レンタルID';
COMMENT ON COLUMN rental.rental_date IS 'レンタル日';
COMMENT ON COLUMN rental.inventory_id IS '在庫ID';
COMMENT ON COLUMN rental.customer_id IS '顧客ID';
COMMENT ON COLUMN rental.return_date IS '返却日';
COMMENT ON COLUMN rental.staff_id IS 'スタッフID';
COMMENT ON COLUMN rental.last_update IS '最終更新日';

COMMENT ON TABLE staff IS 'スタッフ';

COMMENT ON COLUMN staff.staff_id IS 'スタッフID';
COMMENT ON COLUMN staff.first_name IS '名';
COMMENT ON COLUMN staff.last_name IS '姓';
COMMENT ON COLUMN staff.address_id IS '住所ID';
COMMENT ON COLUMN staff.email IS 'メール';
COMMENT ON COLUMN staff.store_id IS '店舗ID';
COMMENT ON COLUMN staff.active IS '有効区分';
COMMENT ON COLUMN staff.username IS 'ユーザー名';
COMMENT ON COLUMN staff.password IS 'パスワード';
COMMENT ON COLUMN staff.last_update IS '最終更新日';
COMMENT ON COLUMN staff.picture IS '画像';

COMMENT ON TABLE store IS '店舗';

COMMENT ON COLUMN store.store_id IS '店舗ID';
COMMENT ON COLUMN store.manager_staff_id IS '店舗マネージャーのスタッフID';
COMMENT ON COLUMN store.address_id IS '住所ID';
COMMENT ON COLUMN store.last_update IS '最終更新日';

