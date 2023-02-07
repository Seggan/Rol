-- ROLMETA {"version":1,"package":"rol","dependencies":[],"variables":[],"classes":[],"interfaces":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
local rol_toString356629
rol_toString356629 = function (x)
        if x == nil then
            return "null"
        else
            return tostring(x)
        end
end
local rol_toNumber356629
rol_toNumber356629 = function (x)
return assertNonNull( tonumber(x) , "file 'convert.rol', line 14, column 11, statement 'extern (x) { tonumber(x) }!'")
end