from exif import Image

RES_DIR = '..\\Resources'

image = None
with open(f'{RES_DIR}\\cat.jpg', 'rb') as f:
    image = Image(f)
    
print(image.has_exif)
print(dir(image))
print(image.image_description)

# image.image_description = 'test'

# with open(f'{RES_DIR}\\image.jpg', 'wb') as f:
#     f.write(image.get_file())