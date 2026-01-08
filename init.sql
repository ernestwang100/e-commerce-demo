-- Create Users Table if not exists (Schema handled by Hibernate usually, but good for init)
CREATE TABLE IF NOT EXISTS `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `is_admin` bit(1) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create Product Table
CREATE TABLE IF NOT EXISTS `product` (
  `id` int NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `quantity` int NOT NULL,
  `retail_price` decimal(38,2) NOT NULL,
  `wholesale_price` decimal(38,2) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create Orders and OrderItem tables (Simplified for dependency)
CREATE TABLE IF NOT EXISTS `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `date_placed` date DEFAULT NULL,
  `order_status` varchar(255) DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Clear existing data if re-running
DELETE FROM `product`;
DELETE FROM `user`;

-- Insert Admin User (password: 123)
-- BCrypt hash for "123": $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd7QA8qkrjm
INSERT INTO `user` (`id`, `email`, `is_admin`, `password`, `role`, `username`) VALUES
(1, 'admin@example.com', 1, '$2a$10$O4Dh30U0.h8EneZj3Xj/vORd1kCr3S.3ZoD/aOhhXVBtcrzdTOS4m', 'ADMIN', 'admin');

-- Insert Regular User (password: 123)
INSERT INTO `user` (`id`, `email`, `is_admin`, `password`, `role`, `username`) VALUES
(2, 'user@example.com', 0, '$2a$10$O4Dh30U0.h8EneZj3Xj/vORd1kCr3S.3ZoD/aOhhXVBtcrzdTOS4m', 'USER', 'user');

-- Insert Demo Products
INSERT INTO `product` (`id`, `name`, `description`, `quantity`, `retail_price`, `wholesale_price`) VALUES
(1, 'Laptop Pro X', 'High performance laptop for developers', 50, 1299.99, 900.00),
(2, 'Wireless Hedgehogs', 'Noise cancelling spikes', 100, 199.50, 120.00),
(3, '4K Monitor', '32-inch Ultra HD display', 30, 450.00, 300.00),
(4, 'Mechanical Keyboard', 'Clicky switches with RGB', 75, 120.00, 80.00),
(5, 'Ergonomic Chair', 'Save your back while you code', 20, 350.00, 200.00),
(6, 'USB-C Hub', 'Add more ports to your life', 200, 45.99, 25.00),
(7, 'Smartphone Z', 'The latest flagship phone', 60, 999.00, 750.00),
(8, 'Tablet Mini', 'Perfect for reading and notes', 80, 299.00, 200.00),
(9, 'Smart Watch', 'Track your health 24/7', 90, 199.99, 130.00),
(10, 'Wireless Mouse', 'Precision sensor and long battery', 150, 59.99, 35.00);
