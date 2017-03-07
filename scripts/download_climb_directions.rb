require 'open-uri'

if ARGV[1] == "d"
	rep_d = true
end
nr = ARGV[0]
data = lambda {open("http://narzew.org/bikeheaven/api/climbs/get_directions_request.php?id=#{nr}"){|s| return s.read }}.call
if rep_d
	data = data.gsub("bicycling", "driving")
end
data = data.split("^api^")[1]
File.open("climb_directions/txt/request#{nr}.txt","wb"){|w| w.write(data) }
print "\nnarzew.org: Request #{nr} complete.\n"
system("curl -o \"climb_directions/json/climb#{nr}.json\" \"#{data}&key=\"")
#open(ata){|gapi| File.open("climb_directions/json/climb#{nr}.json","wb"){|w| w.write(gapi.read) }}
print "\ngoogle api: Request #{nr} complete.\n"
