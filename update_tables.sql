-- Remove address column from users table
ALTER TABLE users DROP COLUMN address;

-- Update any approved-expired status to expired
UPDATE requests SET status = 'expired' WHERE status = 'approved-expired';

-- Update expired requests
UPDATE requests 
SET status = 'expired', request_key = NULL 
WHERE status = 'approved' 
AND expiry_time IS NOT NULL 
AND expiry_time < NOW();