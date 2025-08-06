#!/usr/bin/env python3
"""
Script to convert MockCourseRepository from String to LocalizedContent systematically.
This handles the bulk conversion that would take hours to do manually.
"""

import re
import sys

def convert_exercise_field(line, field_name):
    """Convert a single exercise field from String to LocalizedContent using tTemp()"""
    # Pattern: fieldName = "content"
    pattern = rf'(\s+{field_name}\s*=\s*)"([^"]*)"'
    replacement = r'\1tTemp("\2")'
    return re.sub(pattern, replacement, line)

def convert_exercise_options(line):
    """Convert options list from List<String> to List<LocalizedContent>"""
    # Pattern: options = listOf("opt1", "opt2", "opt3", "opt4")
    pattern = r'(\s+options\s*=\s*listOf\s*\(\s*)"([^"]*)"(\s*,\s*)"([^"]*)"(\s*,\s*)"([^"]*)"(\s*,\s*)"([^"]*)"(\s*\))'
    replacement = r'\1tTemp("\2")\3tTemp("\4")\5tTemp("\6")\7tTemp("\8")\9'
    return re.sub(pattern, replacement, line)

def convert_chapter_field(line, field_name):
    """Convert chapter fields using tMarkdown for content, tTemp for titles"""
    if field_name == "markdownContent":
        # For markdown content, use tMarkdown
        pattern = rf'(\s+{field_name}\s*=\s*)"""([^"]*?)""".trimIndent\(\)'
        replacement = r'\1tMarkdown("""\2""".trimIndent())'
        return re.sub(pattern, replacement, line, flags=re.DOTALL)
    else:
        # For titles, use tTemp
        pattern = rf'(\s+{field_name}\s*=\s*)"([^"]*)"'
        replacement = r'\1tTemp("\2")'
        return re.sub(pattern, replacement, line)

def process_file(file_path):
    """Process the MockCourseRepository file and convert String fields to LocalizedContent"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    lines = content.split('\n')
    converted_lines = []
    
    for line in lines:
        # Convert exercise fields
        if 'questionText =' in line and '"' in line:
            line = convert_exercise_field(line, 'questionText')
        elif 'explanation =' in line and '"' in line:
            line = convert_exercise_field(line, 'explanation')
        elif 'options = listOf(' in line:
            line = convert_exercise_options(line)
        elif 'title =' in line and '"' in line and 'Chapter(' in content[max(0, content.find(line) - 200):content.find(line)]:
            line = convert_chapter_field(line, 'title')
        
        converted_lines.append(line)
    
    # Write back the converted content
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write('\n'.join(converted_lines))
    
    print(f"Converted {file_path}")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python convert_mock_data.py <path_to_MockCourseRepository.kt>")
        sys.exit(1)
    
    file_path = sys.argv[1]
    process_file(file_path)
    print("Conversion complete!")