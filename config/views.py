from django.http import HttpResponse

def health_check(request):
    return HttpResponse("서버가 정상 작동중입니다.")
