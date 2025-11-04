import json
import re

def read_json_file(file_path):
    """读取JSON文件并返回解析后的数据"""
    with open(file_path, 'r', encoding='utf-8') as file:
        return json.load(file)

def format_libraries_as_markdown(libraries_data):
    """将库信息格式化为Markdown无序列表"""
    markdown_lines = []
    libraries = libraries_data.get('libraries', [])
    libraries.sort(key=lambda lib: lib.get('name', '').lower())
    
    for library in libraries:
        name = library.get('name', '')
        if not name:
            continue
            
        version = library.get('artifactVersion', '')
        website = library.get('website', '')
        licenses = library.get('licenses', [])
        
        # 格式化许可证信息
        license_names = []
        for license_item in licenses:
            # 如果license_item是字典，提取name字段
            if isinstance(license_item, dict):
                license_names.append(license_item.get('name', ''))
            # 如果license_item是字符串（ID），则直接使用
            else:
                # 在licenses字典中查找许可证的完整名称
                if license_item in libraries_data.get('licenses', {}):
                    license_names.append(libraries_data['licenses'][license_item].get('name', license_item))
                else:
                    license_names.append(license_item)
        
        # 如果没有找到许可证名称，使用默认值
        if not license_names:
            license_text = "Unknown License"
        else:
            # 过滤掉空的许可证名称
            filtered_license_names = [name for name in license_names if name]
            if not filtered_license_names:
                license_text = "Unknown License"
            else:
                license_text = ", ".join(filtered_license_names)
        
        # 格式化URL - 如果没有网站链接，使用空链接
        url = website if website else ''
        
        # 创建Markdown行
        if url:
            markdown_line = f"- [{name}]({url}) {version} | Under {license_text}"
        else:
            markdown_line = f"- {name} {version} Under {license_text}"
            
        markdown_lines.append(markdown_line)
    
    return markdown_lines

def update_readme_files(markdown_lines):
    """更新README文件中的依赖列表"""
    # 更新英文README (匹配第一个"Click Here to View"区域)
    update_readme('docs/README.md', markdown_lines, r'<summary><strong>Click Here to View</strong></summary>', r'</details>')
    
    # 更新中文README (匹配第一个"点击查看"区域)
    update_readme('docs/README_CN.md', markdown_lines, r'<summary><strong>点击查看</strong></summary>', r'</details>')

def update_readme(file_path, markdown_lines, start_marker, end_marker):
    """更新单个README文件"""
    try:
        # 读取文件内容
        with open(file_path, 'r', encoding='utf-8') as file:
            content = file.read()
        
        # 定义替换区域的正则表达式
        pattern = f'({re.escape(start_marker)}\\s*\\n)(.*?)(\\s*\\n\\s*{re.escape(end_marker)})'
        
        # 生成新的内容
        new_content = '\n'.join(markdown_lines)
        
        # 替换内容
        def replace_func(match):
            return match.group(1) + new_content + match.group(3)
        
        updated_content = re.sub(pattern, replace_func, content, flags=re.DOTALL)
        
        # 写入文件
        with open(file_path, 'w', encoding='utf-8') as file:
            file.write(updated_content)
            
        print(f"Updated {file_path}")
        
    except FileNotFoundError:
        print(f"Error: Cannot find {file_path}")
    except Exception as e:
        print(f"Update {file_path} error: {e}")

def main():
    # 读取JSON文件
    file_path = 'app/src/main/res/raw/aboutlibraries.json'
    try:
        libraries_data = read_json_file(file_path)
        
        # 格式化为Markdown
        markdown_lines = format_libraries_as_markdown(libraries_data)
        
        # 更新README文件
        update_readme_files(markdown_lines)
        
    except FileNotFoundError:
        print(f"Error: Cannot find {file_path}")
    except json.JSONDecodeError:
        print(f"Error: JSON file cannot be parsed {file_path}")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    main()