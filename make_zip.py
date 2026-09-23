import zipfile
import os

zip_path = 'CryptoCycles-v1.133.0-release.aab.zip'
file_to_zip = 'CryptoCycles-v1.133.0.aab'

if not os.path.exists(file_to_zip):
    file_to_zip = 'app/build/outputs/bundle/release/app-release.aab'

with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zf:
    zf.write(file_to_zip, 'CryptoCycles-v1.133.0.aab')
    zf.write(file_to_zip, 'app-release.aab')

print("Successfully created", zip_path)

