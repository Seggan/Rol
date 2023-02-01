-- ROLMETA {"version":1,"package":"rol","dependencies":[],"functions":[{"name":"subs","mangled":"rol_subs6ed8086ed808_6c966_6c966","args":[{"type":"String"},{"type":"Number"},{"type":"Number"}],"returnType":"String"}],"variables":[],"classes":[],"interfaces":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
function rol_subs6ed8086ed808_6c966_6c966(s, starti, endi)
return string.sub(s, starti + 1, endi)
end