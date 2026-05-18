INSERT INTO daily_menus (menu_date, starter, main_course, dessert, price)
VALUES
    (CURRENT_DATE, 'Gazpacho andaluz', 'Paella de marisco', 'Tarta de queso', 18.50),
    (CURRENT_DATE + INTERVAL '1 day', 'Ensalada de quinoa', 'Pollo al horno con verduras', 'Fruta de temporada', 16.90)
ON CONFLICT (menu_date) DO NOTHING;

INSERT INTO reservations (customer_name, phone, reservation_date, reservation_time, people, status)
VALUES
    ('Ana López', '+34600111222', CURRENT_DATE + INTERVAL '1 day', '14:00', 2, 'CONFIRMED'),
    ('Carlos Pérez', '+34600999888', CURRENT_DATE + INTERVAL '2 day', '21:30', 4, 'CONFIRMED');
