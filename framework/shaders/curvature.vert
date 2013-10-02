#version 150
// Default vertex shader

// Uniform variables, set in main program
uniform mat4 projection; 
uniform mat4 modelview;

// Input vertex attributes passed from the main program to shader 
// The position variable corresponds to data passed using
// glDisplayable.addElement(float[], Semantic.POSITION, 3);
in vec4 position;

// The valence variable corresponst to data passed using
// glDisplayable.addElement(float[], Semantic.USERSPECIFIED, 1, "valence")
in float valence;
in float curvature;

//The following would declare and additional variable that could be passed to the shader.
//It would correspond to the data passed via
//glDisplayable.addElement(float[], Semantic.USERDEFINED, 3, "color");
//in vec4 color;

// Output variables are passed to the fragment shader, or, if existent to the geometry shader.
// They have to be declared as in variables in the next shader.
out vec4 frag_color;

void main()
{
	//compute a color and pass it to the fragment shader.
	//frag_color = clamp(abs(position),0,0.75);
	
	frag_color = vec4(log(1+curvature/10.f));
	
	gl_Position = projection * modelview * position;
}
