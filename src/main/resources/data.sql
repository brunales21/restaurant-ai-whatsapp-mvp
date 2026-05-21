INSERT INTO daily_menus (menu_date, starter, main_course, dessert, price)
VALUES
    ('2026-05-21', 'Gazpacho andaluz', 'Paella de marisco', 'Tarta de queso', 18.50),
    ('2026-05-22', 'Ensalada de quinoa', 'Pollo al horno con verduras', 'Fruta de temporada', 16.90),
    ('2026-05-23', 'Croquetas caseras', 'Merluza al horno', 'Arroz con leche', 19.20)
ON CONFLICT (menu_date) DO NOTHING;

INSERT INTO reservations (customer_name, phone, reservation_date, reservation_time, people, status)
VALUES
    ('Ana López', '+34600111222', '2026-05-22', '14:00', 2, 'CONFIRMED'),
    ('Carlos Pérez', '+34600999888', '2026-05-23', '21:30', 4, 'CONFIRMED');
