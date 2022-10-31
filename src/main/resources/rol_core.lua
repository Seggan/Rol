-- START BITWISE OPERATIONS

-- bitwise AND lookup table, 0 to 15
local BIT_AND_LOOKUP = {
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2 },
    { 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 2, 3 },
    { 0, 0, 2, 2, 0, 0, 2, 2, 0, 0, 2, 2, 0, 0, 1, 2 },
    { 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3 },
    { 0, 0, 0, 0, 4, 4, 4, 4, 0, 0, 0, 0, 4, 4, 5, 6 },
    { 0, 1, 0, 1, 4, 5, 4, 5, 0, 1, 0, 1, 4, 5, 6, 7 },
    { 0, 0, 2, 2, 4, 4, 6, 6, 0, 0, 2, 2, 4, 4, 5, 6 },
    { 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7 },
    { 0, 0, 0, 0, 0, 0, 0, 0, 8, 8, 8, 8, 8, 8, 9, 10 },
    { 0, 1, 0, 1, 0, 1, 0, 1, 8, 9, 8, 9, 8, 9, 10, 11 },
    { 0, 0, 2, 2, 0, 0, 2, 2, 8, 8, 10, 10, 8, 8, 9, 10 },
    { 0, 1, 2, 3, 0, 1, 2, 3, 8, 9, 10, 11, 8, 9, 10, 11 },
    { 0, 0, 0, 0, 4, 4, 4, 4, 8, 8, 8, 8, 12, 12, 13, 14 },
    { 0, 1, 0, 1, 4, 5, 4, 5, 8, 9, 8, 9, 12, 13, 14, 15 },
    { 0, 0, 2, 2, 4, 4, 6, 6, 8, 8, 10, 10, 12, 12, 14, 14 },
    { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 }
}

-- bitwise OR lookup table, 0 to 15
local BIT_OR_LOOKUP = {
    { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 },
    { 1, 1, 3, 3, 5, 5, 7, 7, 9, 9, 11, 11, 13, 13, 15, 15 },
    { 2, 3, 2, 3, 6, 7, 6, 7, 10, 11, 10, 11, 14, 15, 14, 15 },
    { 3, 3, 3, 3, 7, 7, 7, 7, 11, 11, 11, 11, 15, 15, 15, 15 },
    { 4, 5, 6, 7, 4, 5, 6, 7, 12, 13, 14, 15, 12, 13, 14, 15 },
    { 5, 5, 7, 7, 5, 5, 7, 7, 13, 13, 15, 15, 13, 13, 15, 15 },
    { 6, 7, 6, 7, 6, 7, 6, 7, 14, 15, 14, 15, 14, 15, 14, 15 },
    { 7, 7, 7, 7, 7, 7, 7, 7, 15, 15, 15, 15, 15, 15, 15, 15 },
    { 8, 9, 10, 11, 12, 13, 14, 15, 8, 9, 10, 11, 12, 13, 14, 15 },
    { 9, 9, 11, 11, 13, 13, 15, 15, 9, 9, 11, 11, 13, 13, 15, 15 },
    { 10, 11, 10, 11, 14, 15, 14, 15, 10, 11, 10, 11, 14, 15, 14, 15 },
    { 11, 11, 11, 11, 15, 15, 15, 15, 11, 11, 11, 11, 15, 15, 15, 15 },
    { 12, 13, 14, 15, 12, 13, 14, 15, 12, 13, 14, 15, 12, 13, 14, 15 },
    { 13, 13, 15, 15, 13, 13, 15, 15, 13, 13, 15, 15, 13, 13, 15, 15 },
    { 14, 15, 14, 15, 14, 15, 14, 15, 14, 15, 14, 15, 14, 15, 14, 15 },
    { 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15 }
}

local BIT_XOR_LOOKUP = {
    { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 },
    { 1, 0, 3, 2, 5, 4, 7, 6, 9, 8, 11, 10, 13, 12, 15, 14 },
    { 2, 3, 0, 1, 6, 7, 4, 5, 10, 11, 8, 9, 14, 15, 12, 13 },
    { 3, 2, 1, 0, 7, 6, 5, 4, 11, 10, 9, 8, 15, 14, 13, 12 },
    { 4, 5, 6, 7, 0, 1, 2, 3, 12, 13, 14, 15, 8, 9, 10, 11 },
    { 5, 4, 7, 6, 1, 0, 3, 2, 13, 12, 15, 14, 9, 8, 11, 10 },
    { 6, 7, 4, 5, 2, 3, 0, 1, 14, 15, 12, 13, 10, 11, 8, 9 },
    { 7, 6, 5, 4, 3, 2, 1, 0, 15, 14, 13, 12, 11, 10, 9, 8 },
    { 8, 9, 10, 11, 12, 13, 14, 15, 0, 1, 2, 3, 4, 5, 6, 7 },
    { 9, 8, 11, 10, 13, 12, 15, 14, 1, 0, 3, 2, 5, 4, 7, 6 },
    { 10, 11, 8, 9, 14, 15, 12, 13, 2, 3, 0, 1, 6, 7, 4, 5 },
    { 11, 10, 9, 8, 15, 14, 13, 12, 3, 2, 1, 0, 7, 6, 5, 4 },
    { 12, 13, 14, 15, 8, 9, 10, 11, 4, 5, 6, 7, 0, 1, 2, 3 },
    { 13, 12, 15, 14, 9, 8, 11, 10, 5, 4, 7, 6, 1, 0, 3, 2 },
    { 14, 15, 12, 13, 10, 11, 8, 9, 6, 7, 4, 5, 2, 3, 0, 1 },
    { 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 }
}

local function bitwiseOp(a, b, lookup)
    local intA = math.floor(a)
    local intB = math.floor(b)
    local result = 0
    local nibble = 1
    while (intA > 0 or intB > 0) do
        local aMod = intA % 16
        local bMod = intB % 16
        intA = math.floor(intA / 16)
        intB = math.floor(intB / 16)
        result = result + lookup[aMod + 1][bMod + 1] * nibble
        nibble = nibble * 16
    end
    return result
end

function bitwiseAnd(a, b)
    return bitwiseOp(a, b, BIT_AND_LOOKUP)
end

function bitwiseOr(a, b)
    return bitwiseOp(a, b, BIT_OR_LOOKUP)
end

function bitwiseXor(a, b)
    return bitwiseOp(a, b, BIT_XOR_LOOKUP)
end

function bitwiseNot(a)
    return -1 - math.floor(a)
end

function bitwiseLeftShift(a, b)
    return math.floor(a) * 2 ^ math.floor(b)
end

function bitwiseRightShift(a, b)
    return math.floor(a) / 2 ^ math.floor(b)
end

-- END BITWISE OPERATIONS
-- START TYPE FUNCTIONS

function typeof(value)
    local type = type(value)
    if type == "table" then
        return type.__clazz
    else
        if type == "number" then
            return "Num"
        elseif type == "string" then
            return "String"
        elseif type == "boolean" then
            return "Boolean"
        elseif type == "nil" then
            return "dyn?"
        else
            return type
        end
    end
end

function assertNonNull(value, location)
    if value == nil then
        error("Expression is null: " .. location, 2)
    else
        return value
    end
end

-- END TYPE FUNCTIONS
-- START TABLE FUNCTIONS

function newTable()
    return {}
end

function setTableValue(table, key, value)
    table[key] = value
end

function getTableValue(table, value)
    return table[value]
end