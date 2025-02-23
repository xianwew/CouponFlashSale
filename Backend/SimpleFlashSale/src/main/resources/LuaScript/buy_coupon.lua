local couponQuantityKey = KEYS[1]
local userOrderSetKey = KEYS[2]

local userId = ARGV[1]

-- Get inventory count
local inventory = tonumber(redis.call("GET", couponQuantityKey))
if not inventory then
    return -3  -- Debugging: Inventory retrieval failed
end

if inventory <= 0 then
    return -1  -- Out of stock
end

if redis.call("SISMEMBER", userOrderSetKey, userId) == 1 then
    return -2  -- Already purchased
end

-- Deduct inventory and record purchase
redis.call("DECR", couponQuantityKey)
redis.call("SADD", userOrderSetKey, userId)

return 1  -- Success
