-- ROLMETA {"version":1,"package":"rol","dependencies":[],"functions":[{"name":"toString","mangled":"rol_toString6ed8085b9d10","args":[{"type":"dyn?"}],"returnType":"String"},{"name":"toNumber","mangled":"rol_toNumber_6c9666ed808","args":[{"type":"String"}],"returnType":"Number"}],"variables":[],"classes":[],"interfaces":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
function rol_toString6ed8085b9d10(x)
if x == nil then
    return "null"
else
    return tostring(x)
end
end
function rol_toNumber_6c9666ed808(x)
return tonumber(x)
end