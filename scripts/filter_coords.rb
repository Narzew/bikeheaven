$coords = []

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
print $new_coords
