# 只在开发模式中被载入
VITE_PORT = 8001

# 网站根目录
VITE_PUBLIC_PATH = /

# 是否开启mock
VITE_USE_MOCK = true

# 网站前缀
VITE_BASE_URL = /

# 是否删除console
VITE_DROP_CONSOLE = true

# 跨域代理，可以配置多个，请注意不要换行
#VITE_PROXY = [["/appApi","http://localhost:8001"],["/upload","http://localhost:8001/upload"]]
# VITE_PROXY=[["/api","https://naive-ui-admin"]]
VITE_PROXY=[["/api","http://localhost:8199/api"],["/api/nomock","http://localhost:8199/api"]]

# API 接口地址
VITE_GLOB_API_URL =

# 图片上传地址
VITE_GLOB_UPLOAD_URL=

# 图片前缀地址
VITE_GLOB_IMG_URL=

# 接口前缀
VITE_GLOB_API_URL_PREFIX = /api



#登录与权限开启
VITE_APP_PM_ENABLED=true
