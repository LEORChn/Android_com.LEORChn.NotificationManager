function main(n)
	init(n)
	local g=n.name..','
	if n.ticker ~= nil then g = g..n.ticker..',' end
	if n.title ~= nil then g = g..n.title..',' end
	if n.desc ~= nil then g = g..n.desc end
	return g
end
-----libs below.
function init(n)
	if n.name == nil then n.name = '' end
	if n.ticker == nil then n.ticker = '' end
	if n.title == nil then n.title = '' end
	if n.desc == nil then n.desc = '' end
	if n.pri == nil then n.pri = 0 end
end