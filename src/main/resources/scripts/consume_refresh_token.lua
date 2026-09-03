---@diagnostic disable: undefined-global
local key = KEYS[1]
local value = redis.call('GET', key)

if value == false then
    return nil
end

local decoded = cjson.decode(value)

if decoded.status == 'active' then
    decoded.status = 'consumed'
    redis.call('SETEX', key, 300, cjson.encode(decoded))
    return value
else
    return 'REUSE_DETECTED:' .. decoded.userId
end
