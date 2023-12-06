#version 330

layout (location = 0) in vec3 attrPosition;
layout (location = 1) in vec4 attrColour;

uniform mat4 uniformProjection;
uniform mat4 uniformView;

out vec4 fColour;

void main()
{
    fColour = attrColour;
    gl_Position = uniformProjection * vec4(attrPosition, 1.0);
}