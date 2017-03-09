Dir.mkdir('climb_directions/fragments_100m') unless Dir.exist?('climb_directions/fragments_100m')
$filename = "climb_directions/coords/climb#{ARGV[0]}.txt"
$new_filename = "climb_directions/fragments_100m/climb#{ARGV[0]}.txt"
if ARGV.size != 1
	print "Split coordinates by 100m\nUsage: split_coords.rb climb_id\n"
end
$coords = []
data = File.read($filename)
data.each_line{|x|
	next if x.size < 3
	next unless x.include?("\x20")
	a = x.split("\x20")
	$coords << [a[0].to_f,a[1].to_f,a[2].to_f]
}
$new_coords = []
total = 0
old_dist = 0
old_ary = []
max_count = $coords[-1][2]
# Anti the same maxcount bug
if max_count%100 == 0
	max_count = max_count+0.01
end
act_nr = 0
count = 0
old_ary = $coords[0]
# Add first coordinate
$new_coords << $coords[0]
$coords.each{|x|
	if x[2] > act_nr
		# Parse variable
		p1 = old_ary
		p2 = x
		n_left = 1/(p2[2]-p1[2])
		n_right = 1-n_left
		print "Warning!" if n_left > 1 or n_right > 1
		average_x = (p1[0]*n_left+p2[0]*n_right)
		average_y = (p1[1]*n_left+p2[1]*n_right)
		act_nr += 100
		$new_coords << [average_x,average_y,act_nr]
		if act_nr > max_count
			# Add last point
			$new_coords[-1] = $coords[-1]
		end
		old_ary = x
		old_dist = x[2]
	else
		old_ary = x
		old_dist = x[2]
	end
}
s = ""
$new_coords.each{|x|
	s << "#{x[0]} #{x[1]} #{x[2]}\n" unless x == nil
	#s << "#{x[0]} #{x[1]} #{x[2]}\n" unless x == nil
}
File.open($new_filename,'wb'){|w| w.write(s) }
