-- ─────────────────────────────────────────────────────────────────────────────
-- Seed data for bookstore_db  (idempotent — uses INSERT ... ON CONFLICT DO NOTHING)
-- ─────────────────────────────────────────────────────────────────────────────

-- Categories
INSERT INTO categories (id, name, description, image_url) VALUES
  (1, 'Fiction',        'Novels, short stories and literary fiction',         'https://picsum.photos/seed/fiction/400/200'),
  (2, 'Non-Fiction',    'Biographies, essays, and general non-fiction',       'https://picsum.photos/seed/nonfiction/400/200'),
  (3, 'Science',        'Physics, biology, chemistry and popular science',    'https://picsum.photos/seed/science/400/200'),
  (4, 'Technology',     'Programming, AI, and software engineering',          'https://picsum.photos/seed/tech/400/200'),
  (5, 'History',        'World history, ancient civilisations, biographies',  'https://picsum.photos/seed/history/400/200'),
  (6, 'Children',       'Picture books, early readers, and young adult',      'https://picsum.photos/seed/children/400/200')
ON CONFLICT (id) DO NOTHING;

SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));

-- Brands (publishers)
INSERT INTO brands (id, name, logo_url) VALUES
  (1, 'Penguin Books',        'https://picsum.photos/seed/penguin/100/100'),
  (2, 'HarperCollins',        'https://picsum.photos/seed/harper/100/100'),
  (3, 'Oxford University Press', 'https://picsum.photos/seed/oup/100/100'),
  (4, 'O''Reilly Media',      'https://picsum.photos/seed/oreilly/100/100'),
  (5, 'Bloomsbury',           'https://picsum.photos/seed/bloomsbury/100/100')
ON CONFLICT (id) DO NOTHING;

SELECT setval('brands_id_seq', (SELECT MAX(id) FROM brands));

-- Products
INSERT INTO products (id, title, author, description, isbn, price, cover_image_url, stock_quantity, pages, published_date, estimated_delivery_days, category_id, brand_id) VALUES
  (1,  'The Great Gatsby',          'F. Scott Fitzgerald', 'A tale of wealth and the American Dream in the 1920s.', '978-0743273565', 9.99,  'https://picsum.photos/seed/gatsby/300/450',    50, 180, '1925-04-10', 4, 1, 1),
  (2,  'To Kill a Mockingbird',     'Harper Lee',          'A story of racial injustice in the American South.',    '978-0061935466', 12.99, 'https://picsum.photos/seed/mockingbird/300/450',50, 336, '1960-07-11', 4, 1, 2),
  (3,  '1984',                      'George Orwell',       'A dystopian novel about totalitarian surveillance.',    '978-0451524935', 10.99, 'https://picsum.photos/seed/1984/300/450',      60, 328, '1949-06-08', 4, 1, 1),
  (4,  'Brave New World',           'Aldous Huxley',       'A vision of a technologically advanced dystopia.',      '978-0060850524', 11.49, 'https://picsum.photos/seed/bnw/300/450',       45, 311, '1932-08-01', 4, 1, 2),
  (5,  'The Hobbit',                'J.R.R. Tolkien',      'A fantasy adventure of hobbits and dragons.',           '978-0547928227', 14.99, 'https://picsum.photos/seed/hobbit/300/450',    70, 310, '1937-09-21', 3, 1, 5),
  (6,  'Harry Potter and the Philosopher''s Stone', 'J.K. Rowling', 'The beginning of the iconic wizard''s journey.', '978-0439708180', 13.99, 'https://picsum.photos/seed/hp1/300/450', 100, 309, '1997-06-26', 3, 1, 5),
  (7,  'Clean Code',                'Robert C. Martin',    'A handbook of agile software craftsmanship.',           '978-0132350884', 39.99, 'https://picsum.photos/seed/cleancode/300/450', 30, 431, '2008-08-11', 3, 4, 4),
  (8,  'The Pragmatic Programmer',  'David Thomas',        'From journeyman to master — a classic for developers.', '978-0135957059', 44.99, 'https://picsum.photos/seed/pragprog/300/450',  25, 352, '2019-09-23', 3, 4, 4),
  (9,  'Sapiens',                   'Yuval Noah Harari',   'A brief history of humankind from evolution to today.', '978-0062316097', 17.99, 'https://picsum.photos/seed/sapiens/300/450',   55, 443, '2015-02-10', 4, 2, 2),
  (10, 'A Brief History of Time',   'Stephen Hawking',     'Landmark exploration of cosmology and black holes.',    '978-0553380163', 15.99, 'https://picsum.photos/seed/hawking/300/450',   40, 212, '1988-04-01', 4, 3, 3),
  (11, 'The Design of Everyday Things', 'Don Norman',      'How design shapes our interaction with the world.',     '978-0465050659', 24.99, 'https://picsum.photos/seed/designthings/300/450', 20, 368, '2013-11-05', 3, 4, 2),
  (12, 'Dune',                      'Frank Herbert',       'An epic science-fiction saga of power and survival.',   '978-0441013593', 16.99, 'https://picsum.photos/seed/dune/300/450',      65, 688, '1965-08-01', 4, 1, 1),
  (13, 'The Alchemist',             'Paulo Coelho',        'A philosophical novel about following your dreams.',    '978-0062315007', 13.49, 'https://picsum.photos/seed/alchemist/300/450', 80, 208, '1988-01-01', 3, 1, 2),
  (14, 'Educated',                  'Tara Westover',       'A memoir of a woman who grows up without formal education.', '978-0399590504', 18.99, 'https://picsum.photos/seed/educated/300/450', 35, 352, '2018-02-20', 4, 2, 2),
  (15, 'Atomic Habits',             'James Clear',         'Tiny changes, remarkable results — a guide to habits.', '978-0735211292', 21.99, 'https://picsum.photos/seed/atomichabits/300/450', 90, 320, '2018-10-16', 3, 2, 2)
ON CONFLICT (id) DO NOTHING;

SELECT setval('products_id_seq', (SELECT MAX(id) FROM products));
