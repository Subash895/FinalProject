CREATE TABLE `users` (
  `id` integer PRIMARY KEY,
  `name` varchar(255),
  `email` varchar(255) UNIQUE,
  `password` varchar(255),
  `role` varchar(255),
  `created_at` timestamp
);

CREATE TABLE `user_profile` (
  `id` integer PRIMARY KEY,
  `user_id` integer UNIQUE NOT NULL,
  `phone_number` varchar(255),
  `address` text,
  `profession` varchar(255)
);

CREATE TABLE `businesses` (
  `id` integer PRIMARY KEY,
  `name` varchar(255),
  `owner_name` varchar(255),
  `category` varchar(255),
  `address` text,
  `is_premium` boolean,
  `created_at` timestamp,
  `user_id` integer
);

CREATE TABLE `places` (
  `id` integer PRIMARY KEY,
  `name` varchar(255),
  `category` varchar(255),
  `description` text,
  `latitude` double,
  `longitude` double,
  `created_at` timestamp
);

CREATE TABLE `city_history` (
  `id` integer PRIMARY KEY,
  `title` varchar(255),
  `content` text,
  `category` varchar(255),
  `year` integer
);

CREATE TABLE `news` (
  `id` integer PRIMARY KEY,
  `title` varchar(255),
  `content` text,
  `category` varchar(255),
  `is_government_notice` boolean,
  `published_date` timestamp
);

CREATE TABLE `forum_topics` (
  `id` integer PRIMARY KEY,
  `title` varchar(255),
  `user_id` integer,
  `created_at` timestamp
);

CREATE TABLE `forum_replies` (
  `id` integer PRIMARY KEY,
  `topic_id` integer,
  `user_id` integer,
  `message` text,
  `created_at` timestamp
);

CREATE TABLE `subscriptions` (
  `id` integer PRIMARY KEY,
  `user_id` integer,
  `service_name` varchar(255),
  `amount` decimal,
  `status` varchar(255),
  `created_at` timestamp
);

CREATE TABLE `payments` (
  `id` integer PRIMARY KEY,
  `user_id` integer,
  `subscription_id` integer,
  `amount` decimal,
  `payment_method` varchar(255),
  `payment_status` varchar(255),
  `payment_date` timestamp
);

CREATE TABLE `advertisement` (
  `id` integer PRIMARY KEY,
  `business_id` integer,
  `title` varchar(255),
  `image_url` varchar(255),
  `start_date` date,
  `end_date` date,
  `is_active` boolean
);

CREATE TABLE `market_rates` (
  `id` integer PRIMARY KEY,
  `product_name` varchar(255),
  `price` decimal,
  `unit` varchar(255),
  `update_date` timestamp
);

ALTER TABLE `user_profile` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `businesses` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `forum_topics` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `forum_replies` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `forum_replies` ADD FOREIGN KEY (`topic_id`) REFERENCES `forum_topics` (`id`);

ALTER TABLE `subscriptions` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `payments` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `payments` ADD FOREIGN KEY (`subscription_id`) REFERENCES `subscriptions` (`id`);

ALTER TABLE `advertisement` ADD FOREIGN KEY (`business_id`) REFERENCES `businesses` (`id`);
