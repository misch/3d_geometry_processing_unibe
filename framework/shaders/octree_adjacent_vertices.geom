#version 150
// input: an octree center and a side length
// output: linesegments outlining the octree cube

uniform mat4 projection; 
uniform mat4 modelview;

layout(points) in;
layout(line_strip, max_vertices = 2) out;

in vec4 position_g[];
in vec4 pointTo_g[];
out vec4 color_g;

void main()
{			
	gl_PrimitiveID = gl_PrimitiveIDIn;
	color_g = vec4(0);
	
	gl_Position = projection*modelview*(position_g[0]);
	EmitVertex();
	
	vec4 goDirection = pointTo_g[0] - position_g[0];
	gl_Position = projection*modelview*(position_g[0] + goDirection/3); 
	EmitVertex();
	
}
