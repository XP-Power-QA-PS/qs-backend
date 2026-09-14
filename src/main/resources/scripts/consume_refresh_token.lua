---@diagnostic disable: undefined-global
local key = KEYS[1]
local value = redis.call('GET', key)

if value == false then
    return nil
end

local decoded = cjson.decode(value)
local currentTime = tonumber(redis.call('TIME')[1])

if decoded.status == 'active' then
    decoded.status = 'consumed'
    decoded.consumedAt = currentTime
    redis.call('SETEX', key, 300, cjson.encode(decoded))
    return value
else
    -- Grace period: allow repeated refresh within 15 seconds to tolerate multi-tab concurrency
    local consumedAt = tonumber(decoded.consumedAt)
    if consumedAt and (currentTime - consumedAt) <= 15 then
        return value
    else
        return 'REUSE_DETECTED:' .. decoded.userId
    end
end

