from rest_framework import generics, status
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework.permissions import AllowAny, IsAuthenticated
from django.contrib.auth import authenticate, login, logout
from .serializers import UserSerializer
from .models import User
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer

class UserRegisterView(generics.CreateAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer
    permission_classes = [AllowAny]

class UserLoginView(APIView):
    permission_classes = [AllowAny]

    def post(self, request, *args, **kwargs):
        serializer = TokenObtainPairSerializer(data=request.data)
        try:
            serializer.is_valid(raise_exception=True)
        except Exception as e:
            return Response({'detail': str(e)}, status=status.HTTP_401_UNAUTHORIZED)
        return Response(serializer.validated_data, status=status.HTTP_200_OK)

class UserLogoutView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        # For JWT, logout typically means client-side token removal.
        # Server-side logout might involve blacklisting tokens, which simplejwt supports.
        # For now, a simple success response is sufficient.
        return Response({'message': 'Successfully logged out (client-side token removal recommended).'}, status=status.HTTP_200_OK)

class UserProfileView(generics.RetrieveUpdateAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer
    permission_classes = [IsAuthenticated]

    def get_object(self):
        return self.request.user

class UserPasswordChangeView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        user = request.user
        old_password = request.data.get('old_password')
        new_password = request.data.get('new_password')

        if not user.check_password(old_password):
            return Response({'error': 'Old password is not correct.'}, status=status.HTTP_400_BAD_REQUEST)

        user.set_password(new_password)
        user.save()
        return Response({'message': 'Password changed successfully.'}, status=status.HTTP_200_OK)