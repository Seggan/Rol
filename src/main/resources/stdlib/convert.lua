-- ROLMETA {"version":1,"package":"rol","dependencies":[],"variables":[{"name":"toString","mangled":"rol_toString1884ff","type":"(dyn?) -> String"},{"name":"toNumber","mangled":"rol_toNumber1884ff","type":"(String) -> Number"}],"classes":[],"interfaces":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
rol_toString1884ff = function (x)
        if x == nil then
            return "null"
        else
            return tostring(x)
        end
end
rol_toNumber1884ff = function (x)
return assertNonNull( tonumber(x) , "file 'convert.rol', line 14, column 11, statement 'extern (x) { tonumber(x) }!'")
end