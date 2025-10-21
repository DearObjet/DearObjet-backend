from rest_framework import serializers
from .models import User, BusinessInfo, TermsAgreement

class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('id', 'userId', 'userType', 'phoneNumber', 'name', 'profileImage', 'isActive', 'createdAt', 'updatedAt')
        read_only_fields = ('createdAt', 'updatedAt')

class BusinessInfoSerializer(serializers.ModelSerializer):
    class Meta:
        model = BusinessInfo
        fields = '__all__'

class TermsAgreementSerializer(serializers.ModelSerializer):
    class Meta:
        model = TermsAgreement
        fields = '__all__'
