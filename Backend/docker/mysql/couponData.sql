USE SimpleFlashSale;
INSERT INTO coupons (name, imageURL, price, description, quantity, created_at, is_deleted)
VALUES (
    'VIP Concert Ticket', 
    'https://example.com/ticket.jpg', 
    150, 
    'Exclusive VIP ticket for the annual music festival.', 
    100, 
    NOW(), 
    FALSE
);