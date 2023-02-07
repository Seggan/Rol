-- ROLMETA {"version":1,"package":"rol","dependencies":[],"variables":[],"classes":[],"interfaces":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
local rol_subs6c5ffc
rol_subs6c5ffc = function (s, starti, endi)
return assertNonNull( string.sub(s, starti + 1, endi) , "file 'rstring.rol', line 4, column 11, statement 'extern (s, starti, endi) { string.sub(s, starti + 1, endi) }!'")
end