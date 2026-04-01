CREATE TABLE `user_profile`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL COMMENT 'forign key',
    `phone_number` VARCHAR(255) NOT NULL,
    `adress` TEXT NOT NULL,
    `profession` VARCHAR(255) NOT NULL
);
CREATE TABLE `users`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `role` ENUM('') NOT NULL,
    `created_at` TIMESTAMP NOT NULL
);
CREATE TABLE `places`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `category` ENUM(
        '' TOURIST '',
        '' MARKET '',
        '' HOSPITAL '',
        '' TRANSPORT '',
        '' GOVERNMENT '',
        '' OTHER ''
    ) NOT NULL,
    `destination` TEXT NOT NULL,
    `latitude` DOUBLE NOT NULL,
    `longitude` DOUBLE NOT NULL,
    `created_at` TIMESTAMP NOT NULL
);
CREATE TABLE `city_history`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL,
    `content` TEXT NOT NULL,
    `category` VARCHAR(255) NOT NULL,
    `year` INT NOT NULL
);
CREATE TABLE `businesses`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `owner_name` VARCHAR(255) NOT NULL,
    `category` VARCHAR(255) NOT NULL,
    `address` TEXT NOT NULL,
    `is_premium` BOOLEAN NOT NULL,
    `created_at` TIMESTAMP NOT NULL,
    `user_id` INT NOT NULL COMMENT 'users.id (forign key)'
);
CREATE TABLE `news`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL,
    `category` VARCHAR(255) NOT NULL,
    `is_government_notice` BOOLEAN NOT NULL,
    `published_date` TIMESTAMP NOT NULL
);
CREATE TABLE `forum_topic`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'FK → users.id',
    `title` VARCHAR(255) NOT NULL,
    `user_id` INT NOT NULL,
    `created_at` TIMESTAMP NOT NULL
);
CREATE TABLE `forum_replies`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `topic_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `message` TEXT NOT NULL,
    `created_at` TIMESTAMP NOT NULL
);
CREATE TABLE `subcriptions`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `service_name` VARCHAR(255) NOT NULL,
    `amount` DECIMAL(8, 2) NOT NULL,
    `status` ENUM('(ENUM ' pending '', '' completed ')') NOT NULL,
    `created_at` TIMESTAMP NOT NULL
);
CREATE TABLE `advertisement`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `business_id` INT NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `image_url` VARCHAR(255) NOT NULL,
    `start_date` DATE NOT NULL,
    `end_date` DATE NOT NULL,
    `is_active` BOOLEAN NOT NULL
);
CREATE TABLE `market_rates`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `product_name` VARCHAR(255) NOT NULL,
    `price` DECIMAL(8, 2) NOT NULL,
    `unit` VARCHAR(255) NOT NULL,
    `update_date` TIMESTAMP NOT NULL
);
CREATE TABLE `payments`(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `subscription_id` INT NOT NULL,
    `amount` DECIMAL(8, 2) NOT NULL,
    `payment_method` VARCHAR(255) NOT NULL,
    `payment_status` ENUM(
        '(ENUM ' success '',
        '' failed '',
        '' pending ')'
    ) NOT NULL,
    `payment_date` TIMESTAMP NOT NULL
);
ALTER TABLE
    `forum_replies` ADD CONSTRAINT `forum_replies_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `users`(`id`);
ALTER TABLE
    `payments` ADD CONSTRAINT `payments_subscription_id_foreign` FOREIGN KEY(`subscription_id`) REFERENCES `subcriptions`(`id`);
ALTER TABLE
    `payments` ADD CONSTRAINT `payments_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `users`(`id`);
ALTER TABLE
    `advertisement` ADD CONSTRAINT `advertisement_business_id_foreign` FOREIGN KEY(`business_id`) REFERENCES `businesses`(`id`);
ALTER TABLE
    `subcriptions` ADD CONSTRAINT `subcriptions_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `users`(`id`);
ALTER TABLE
    `user_profile` ADD CONSTRAINT `user_profile_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `users`(`id`);
ALTER TABLE
    `businesses` ADD CONSTRAINT `businesses_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `users`(`id`);
ALTER TABLE
    `forum_topic` ADD CONSTRAINT `forum_topic_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `users`(`id`);
ALTER TABLE
    `forum_replies` ADD CONSTRAINT `forum_replies_topic_id_foreign` FOREIGN KEY(`topic_id`) REFERENCES `forum_topic`(`id`);