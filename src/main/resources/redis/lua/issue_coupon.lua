-- KEYS[1] = 쿠폰 큐 (List)
-- KEYS[2] = 발급 내역 저장 Hash
-- ARGV[1] = 유저 ID

local queueKey = KEYS[1]
local issuedKey = KEYS[2]
local userId = ARGV[1]

-- 이미 발급받은 사용자 확인
if redis.call("HEXISTS", issuedKey, userId) == 1 then
  return "DUPLICATE"
end

-- 쿠폰 큐에서 꺼내기
local couponKey = redis.call("RPOP", queueKey)
if not couponKey then
  return "SOLD_OUT"
end

-- 발급 내역 기록
local data = cjson.encode({
  couponKey = couponKey,
  userId = userId,
  issuedAt = tostring(redis.call("TIME")[1]),
  status = "PENDING"
})
redis.call("HSET", issuedKey, userId, data)

return couponKey
