-- Redis Keys
local couponQuantityKey = KEYS[1]  -- "SimpleFlashSale#coupon:123:quantity"
local userOrderSetKey = KEYS[2]    -- "SimpleFlashSale#couponUsers:123"

-- User ID
local userId = ARGV[1]

-- Restore stock: Increment quantity
redis.call("INCR", couponQuantityKey)

-- Remove user from the order set
redis.call("SREM", userOrderSetKey, userId)

-- Return success
return 1
