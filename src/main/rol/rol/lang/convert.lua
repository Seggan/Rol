-- ROLMETA {"version":1,"package":"rol.lang","functions":[{"name":"toString","mangled":"toString_rol_lang_cf0c6_626e1x2f48a878","args":[{"name":"x","type":"dyn?"}],"returnType":"String"},{"name":"toNumber","mangled":"toNumber_rol_lang25395a22d986x_cf0c678","args":[{"name":"x","type":"String"}],"returnType":"Num"}],"variables":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
function toString_rol_lang_cf0c6_626e1x2f48a878(x)

    if x == nil then
        return "null"
    else
        return tostring(x)
    end
end
function toNumber_rol_lang25395a22d986x_cf0c678(x)
return tonumber(x)
end