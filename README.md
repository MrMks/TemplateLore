# TemplateLore

This is a simple plugin to generate an item in minecraft.

## Configs
To generate the item, you should write a template for it.  
Besides the template, you can also write some word stocks for easier management.

For template, please see [/resources/template.yml](./src/main/resources/template.yml)  
For wordStock, please see [/resources/wordStock.yml](./src/main/resources/wordStock.yml)

Just put your template files in template folder and put your wordStock files in wordStock folder

## Tags

Here are the parse tags, start with '<' and end with '>', with an ':' to split the key and the value;

|key|value|description|
|:---|:---|:---|
|r|a,b|generate an random integer between a and b. both are include|
|c|a,b|copy the string a for b times, the str can't be space|
|C|b|copy a space for b times|
|f|a,b|use the pattern b to format the number a|
|s|k|random select a word in wordStock k|
|l|k|random select a word in wordStock k then remember the parsed word|
|L|k|random select a word in wordStock k then remember the source word|
|m(in beta)|formula|cal the given formula and return as an double value|
|p|papi_code|parse the given papi code, without the '%' mark|

