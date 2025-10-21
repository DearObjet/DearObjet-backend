from rest_framework.routers import DefaultRouter
from .views import ShopViewSet, ArtistViewSet, ProductViewSet

router = DefaultRouter()
router.register(r'shops', ShopViewSet)
router.register(r'artists', ArtistViewSet)
router.register(r'products', ProductViewSet)

urlpatterns = router.urls
