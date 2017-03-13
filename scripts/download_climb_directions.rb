require 'open-uri'

if ARGV.include?("-walking")
	$go_mode = "walking"
elsif ARGV.include?("-bicycling")
	$go_mode = "bicycling"
elsif ARGV.include?("-driving")
	$go_mode = "driving"
else
	$go_mode = "driving"
end
nr = ARGV[0]
data = lambda {open("http://narzew.org/bikeheaven/api/climbs/get_directions_request.php?id=#{nr}"){|s| return s.read }}.call
data = data.gsub("bicycling", $go_mode)
data = data.split("^api^")[1]
File.open("climb_directions/txt/request#{nr}.txt","wb"){|w| w.write(data) }
print "\nnarzew.org: Request #{nr} complete.\n"
#system("curl -o \"climb_directions/json/climb#{nr}.json\" \"#{data}\"")
system("curl -o \"climb_directions/json/climb#{nr}.json\" \"#{data}&key=secret\"") # My API Key
#open(ata){|gapi| File.open("climb_directions/json/climb#{nr}.json","wb"){|w| w.write(gapi.read) }}
print "\ngoogle api: Request #{nr} complete.\n"
