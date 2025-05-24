import os

# Configuration
SRC_DIRECTORY = "src"
OUTPUT_FILENAME = "combined_src_content.txt"
FILE_HEADER_TEMPLATE = "\n\n--- Content of: {filepath} ---\n\n"

def get_all_files_content():
    """
    Traverses the SRC_DIRECTORY, reads the content of all files,
    and writes it into a single OUTPUT_FILENAME.
    """
    if not os.path.isdir(SRC_DIRECTORY):
        print(f"Error: Source directory '{SRC_DIRECTORY}' not found in the current location.")
        print(f"Please ensure the script is in the parent directory of '{SRC_DIRECTORY}'.")
        return

    all_content_parts = []
    files_processed_count = 0
    files_error_count = 0

    print(f"Starting to scan and read files from '{SRC_DIRECTORY}'...")

    for root, _, files in os.walk(SRC_DIRECTORY):
        for filename in files:
            filepath = os.path.join(root, filename)
            
            all_content_parts.append(FILE_HEADER_TEMPLATE.format(filepath=filepath))
            
            try:
                with open(filepath, 'r', encoding='utf-8', errors='surrogateescape') as f:
                    content = f.read()
                    all_content_parts.append(content)
                print(f"Successfully read: {filepath}")
                files_processed_count += 1
            except Exception as e:
                error_message = f"Error reading file {filepath}: {e}"
                all_content_parts.append(f"{error_message}\n")
                print(error_message)
                files_error_count += 1

    if not all_content_parts:
        print(f"No files found or no content could be read from '{SRC_DIRECTORY}'.")
        return

    print(f"\nCombining content of {files_processed_count} file(s)...")
    
    try:
        with open(OUTPUT_FILENAME, 'w', encoding='utf-8') as outfile:
            outfile.write("".join(all_content_parts))
        
        print(f"\nSuccessfully generated '{OUTPUT_FILENAME}'.")
        print(f"Total files processed: {files_processed_count}")
        if files_error_count > 0:
            print(f"Encountered errors while reading {files_error_count} file(s).")
            print(f"Check '{OUTPUT_FILENAME}' for specific error messages at the respective file sections.")
        else:
            print("All readable files processed successfully.")
            
    except Exception as e:
        print(f"Error writing to output file '{OUTPUT_FILENAME}': {e}")

if __name__ == "__main__":
    get_all_files_content()