package com.example.threes;

import java.util.Random;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.widget.TextView;

public class Play extends Activity {

	// 좌표 변수
	private PointF beganPoint;
	private PointF prevPoint;
	private PointF currentPoint;
	private PointF nextDigitStartPoint;
	private PointF panBasePoint[][];
	private PointF panPoint[][];

	// TextView 변수
	private TextView viewPoint;
	private TextView viewAction;
	private TextView viewDirection;
	private TextView viewNextDigit;

	// pan 변수
	private TextView pan[][];

	// !임시 확인용
	private TextView viewDigitState;

	// 변수
	boolean isInit;
	boolean isMoveFirst; // 터치 후 처음으로 움직임이 발생했는지 확인 변수
	int directionState; // 사용자의 swipe 방향
	int digitArray[][]; // 패널 위의 숫자 관리 배열
	boolean okNewDigit[];
	int nextDigit;
	float dx; // x좌표의 이동거리
	float dy; // y좌표의 이동거리
	double moveLength; // 드래그해서 이동안 거리의 길이
	int highDigit; // 가장 높은 숫자 저장
	int digitManager[];
	boolean isMove;
	float xDistance; // 패널사이의 간격
	float yDistance;
	boolean check;
	int isBack;
	// 터치 동작 제한 변수
	boolean after;	// 업 터치 이벤트 확인 변수
	boolean inDown;	// 다운 터치 이벤트 확인 변수
	boolean inMove;	// 무브 터치 이벤트 확인 변수

	// 상수
	final static int TOP_DIRECTION = 1;
	final static int BOTTOM_DIRECTION = 2;
	final static int RIGHT_DIRECTION = 3;
	final static int LEFT_DIRECTION = 4;
	final static int MAX_DISTANCE = 150;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play);

		// 멤버 변수 초기화
		initPlay();

		// !임시 확인용
		viewDigitState = (TextView) findViewById(R.id.viewDigitState);

		viewPan();
		nextDigit = -1;
		nextDigit = createNextDigit();
	}

	private void initPlay() {

		beganPoint = new PointF();
		prevPoint = new PointF();
		currentPoint = new PointF();
		nextDigitStartPoint = new PointF();
		panBasePoint = new PointF[4][4];
		panPoint = new PointF[4][4];
		pan = new TextView[4][4];
		digitArray = new int[4][4];
		okNewDigit = new boolean[4];
		digitManager = new int[2];
		digitManager[0] = 0;
		digitManager[1] = 0;
		highDigit = 6;

		viewPoint = (TextView) findViewById(R.id.viewPoint);
		viewAction = (TextView) findViewById(R.id.viewAction);
		viewDirection = (TextView) findViewById(R.id.viewDirection);
		viewNextDigit = (TextView) findViewById(R.id.viewNextDigit);

		// pan
		pan[0][0] = (TextView) findViewById(R.id.pan11);
		pan[0][1] = (TextView) findViewById(R.id.pan12);
		pan[0][2] = (TextView) findViewById(R.id.pan13);
		pan[0][3] = (TextView) findViewById(R.id.pan14);
		pan[1][0] = (TextView) findViewById(R.id.pan21);
		pan[1][1] = (TextView) findViewById(R.id.pan22);
		pan[1][2] = (TextView) findViewById(R.id.pan23);
		pan[1][3] = (TextView) findViewById(R.id.pan24);
		pan[2][0] = (TextView) findViewById(R.id.pan31);
		pan[2][1] = (TextView) findViewById(R.id.pan32);
		pan[2][2] = (TextView) findViewById(R.id.pan33);
		pan[2][3] = (TextView) findViewById(R.id.pan34);
		pan[3][0] = (TextView) findViewById(R.id.pan41);
		pan[3][1] = (TextView) findViewById(R.id.pan42);
		pan[3][2] = (TextView) findViewById(R.id.pan43);
		pan[3][3] = (TextView) findViewById(R.id.pan44);

		isMoveFirst = false;
		directionState = 0;
		nextDigit = 0;
		inDown = false;
		inMove = false;
		isInit = true;
		after = false;
		

		// 배열 초기화
		int count = 0;
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++) {
				if (count < 8) { // 처음 생성되는 숫자가 포함된 패널의 수
					digitArray[i][j] = (int) (Math.random() * 10 % 4);
					if (digitArray[i][j] != 0)
						count++;
				}
			}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		// 현재 터치 액션의 종류 확인
		int action = event.getAction();

		// 이벤트가 발생된 지점 좌표 저장
		float x = event.getX();
		float y = event.getY();

		// 터치에 따른 이벤트 처리
		if (!after) {	// 이 전의 모든 쓰레드 애니메이션이 종료된 후 터치 이벤트가 진행된다
			if (action == MotionEvent.ACTION_DOWN) { // 화면을 터치했을 경우
				viewAction.setText("Action Down");
				viewPoint.setText("X: " + x + "\nY: " + y);
				viewDirection.setText("DIRECTION");

				
				nextDigitStartPoint.set(viewNextDigit.getX(), viewNextDigit.getY());
				
				touchDown(x, y);
			} 
			else if (action == MotionEvent.ACTION_MOVE) { // 화면을 드래그 했을 경우
				viewAction.setText("Action Move");
				viewPoint.setText("X: " + x + "\nY: " + y);
				
				touchMove(x, y);
			} else if (action == MotionEvent.ACTION_UP) {
				viewAction.setText("Action Up");
				
				touchUp();
			}
		}
		return super.onTouchEvent(event);
	}
	
	void touchDown(float x, float y) {
		// 최초 한 번 오브젝트 베이스 좌표 저장
		if (isInit) {
			for (int i = 0; i < 4; i++)
				for (int j = 0; j < 4; j++)
					panBasePoint[i][j] = new PointF(pan[i][j].getX(), pan[i][j].getY());

			// 패널 사이의 간격 구하기
			xDistance = getInterval(pan[0][1].getX(), pan[0][0].getX());
			yDistance = getInterval(pan[1][0].getY(), pan[0][0].getY());
			isInit = false;
		}

		isMove = false;
		inDown = true;

		// 방향 초기화
		directionState = 0;

		// 사용자가 터치한 지점 좌표 저장
		beganPoint.set(x, y);

		// 이전 좌표를 사용자가 최초로 터치한 지점의 좌표로
		prevPoint.set(beganPoint);
	}
	
	void touchMove(float x, float y) {
		if (inDown) {	// touchDown된 후에만 아래 코드 동작
			inMove = true;

			// 움직인 지점 좌표 저장
			currentPoint.set(x, y);

			// 현재의 오브젝트 좌표 저장
			PointF viewNextDigitPoint = new PointF(viewNextDigit.getX(), viewNextDigit.getY());
			for (int i = 0; i < 4; i++)
				for (int j = 0; j < 4; j++)
					panPoint[i][j] = new PointF(pan[i][j].getX(), pan[i][j].getY());

			// 각 좌표 이동 거리
			dx = getInterval(currentPoint.x, prevPoint.x);
			dy = getInterval(currentPoint.y, prevPoint.y);

			// 일정 거리 이상 드래그했을 시에만 판정
			moveLength = Math.sqrt(Math.pow(getInterval(currentPoint.x, beganPoint.x), 2)
							+ Math.pow(getInterval(currentPoint.y, beganPoint.y), 2));

			if (moveLength > 50) {	// 드래그한 거리가 50이상일때 
				// 터치한 후 처음으로 움직였다면
				if (isMoveFirst == false) {

					// 각도를 구한다
					double radian = Math.atan2(dy, dx);
					double degree = (radian * 180) / Math.PI;

					// 각도에 따른 방향 설정
					if (degree > 45 && degree <= 135) {
						directionState = BOTTOM_DIRECTION;
						viewDirection.setText("BOTTOM");
					} else if ((degree > 135 && degree < 181)
							|| (degree <= -135 && degree > -180)) {
						directionState = LEFT_DIRECTION;
						viewDirection.setText("LEFT");
					} else if (degree > -135 && degree <= -45) {
						directionState = TOP_DIRECTION;
						viewDirection.setText("TOP");
					} else {
						directionState = RIGHT_DIRECTION;
						viewDirection.setText("RIGHT");
					}

					isMoveFirst = true;
				}

				// 방향에 따른 오브젝트 이동
				float fx = viewNextDigitPoint.x;
				float fy = viewNextDigitPoint.y;

				switch (directionState) {
				case RIGHT_DIRECTION:
					if ((nextDigitStartPoint.x < fx + dx))
						fx += dx;
					moveRightFunc();
					break;

				case TOP_DIRECTION:
					if (nextDigitStartPoint.y > fy + dy)
						fy += dy;
					moveTopFunc();
					break;

				case LEFT_DIRECTION:
					if (nextDigitStartPoint.x > fx + dx)
						fx += dx;
					moveLeftFunc();
					break;

				case BOTTOM_DIRECTION:
					if (nextDigitStartPoint.y < fy + dy)
						fy += dy;
					moveBottomFunc();
					break;

				default:
					break;
				}

				// 오브젝트 이동
				viewNextDigit.setX(fx);
				viewNextDigit.setY(fy);
			}

			// 이전 좌표 저장
			prevPoint.set(currentPoint);
		}
	}
	
	void touchUp() {
		if (inMove) {	// touchMove된 후에만 아래 코드 동작
			// 일정 거리 이상 움직였을 시 결과 반영
			// PanMoveThread로 애니메이션 효과
			if (isMove) {
				after = true;
				switch (directionState) {
				case RIGHT_DIRECTION: {
					PanMoveThread th;
					if (beganPoint.x + (xDistance / 2) <= currentPoint.x)
						th = new PanMoveThread(mHandler, RIGHT_DIRECTION, 0);
					else
						th = new PanMoveThread(mHandler, RIGHT_DIRECTION, 1);
					th.start();
					break;
				}
				case LEFT_DIRECTION: {
					PanMoveThread th;
					if (beganPoint.x - (xDistance / 2) >= currentPoint.x)
						th = new PanMoveThread(mHandler, LEFT_DIRECTION, 0);
					else
						th = new PanMoveThread(mHandler, LEFT_DIRECTION, 1);
					th.start();
					break;
				}
				case BOTTOM_DIRECTION: {
					PanMoveThread th;
					if (beganPoint.y + (yDistance / 2) - 10 <= currentPoint.y)
						th = new PanMoveThread(mHandler, BOTTOM_DIRECTION, 0);
					else
						th = new PanMoveThread(mHandler, BOTTOM_DIRECTION, 1);
					th.start();
					break;
				}
				case TOP_DIRECTION: {
					PanMoveThread th;
					if (beganPoint.y - (yDistance / 2) + 10 >= currentPoint.y)
						th = new PanMoveThread(mHandler, TOP_DIRECTION, 0);
					else
						th = new PanMoveThread(mHandler, TOP_DIRECTION, 1);
					th.start();
					break;
				}
				}
			} else
				viewPoint .setText("-Now, no more panels\n-can not move in that direction");
			viewDirection.setText("DIRECTION");

			// 초기화
			isMoveFirst = false;
			inDown = false;
			inMove = false;
			
			// 오브젝트 제자리로
			viewNextDigit.setX(nextDigitStartPoint.x);
			viewNextDigit.setY(nextDigitStartPoint.y);
		}
	}

	// 쓰레드 핸들러
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			switch (msg.what) {
			case (RIGHT_DIRECTION * 10 + 0): {	// 오른쪽 합치기
				boolean selfCheck = false;
				for (int i = 0; i < 4; i++) {
					for (int j = 0; j < 3; j++) {
						float x = pan[i][j].getX();
						if ((x != panBasePoint[i][j].x)
								&& (x < panBasePoint[i][j + 1].x)) {
							pan[i][j].setX(x + 1);
							selfCheck = true;
						}
					}
				}
				if (!selfCheck) {
					check = false;
				}
				break;
			}
			case (RIGHT_DIRECTION * 10 + 1): {	// 오른쪽 돌아가기
				boolean selfCheck = false;
				for (int i = 0; i < 4; i++) {
					for (int j = 0; j < 3; j++) {
						float x = pan[i][j].getX();
						if (x > panBasePoint[i][j].x) {
							pan[i][j].setX(x - 1);
							selfCheck = true;
						}
					}
				}
				if (!selfCheck)
					check = false;
				break;
			}
			case (LEFT_DIRECTION * 10 + 0): {	// 왼쪽 합치기
				boolean selfCheck = false;
				for (int i = 3; i >= 0; i--) {
					for (int j = 3; j > 0; j--) {
						float x = pan[i][j].getX();
						if ((x != panBasePoint[i][j].x)
								&& (x > panBasePoint[i][j - 1].x)) {
							pan[i][j].setX(x - 1);
							selfCheck = true;
						}
					}
				}
				if (!selfCheck)
					check = false;
				break;
			}
			case (LEFT_DIRECTION * 10 + 1): {	// 왼쪽 돌아가기
				boolean selfCheck = false;
				for (int i = 3; i >= 0; i--) {
					for (int j = 3; j > 0; j--) {
						float x = pan[i][j].getX();
						if (x < panBasePoint[i][j].x) {
							pan[i][j].setX(x + 1);
							selfCheck = true;
						}
					}
				}
				if (!selfCheck)
					check = false;
				break;
			}
			case (BOTTOM_DIRECTION * 10 + 0): {	// 아래쪽 합치기
				boolean selfCheck = false;
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 4; j++) {
						float y = pan[i][j].getY();
						if ((y != panBasePoint[i][j].y)
								&& (y < panBasePoint[i + 1][j].y)) {
							pan[i][j].setY(y + 1);
							selfCheck = true;
						}
					}
				}
				if (!selfCheck)
					check = false;
				break;
			}
			case (BOTTOM_DIRECTION * 10 + 1): {	// 아래쪽 돌아가기
				boolean selfCheck = false;
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 4; j++) {
						float y = pan[i][j].getY();
						if (y > panBasePoint[i][j].y) {
							pan[i][j].setY(y - 1);
							selfCheck = true;
						}
					}
				}
				if (!selfCheck)
					check = false;
				break;
			}
			case (TOP_DIRECTION * 10 + 0): {	// 위쪽 합치기
				boolean selfCheck = false;
				for (int i = 3; i > 0; i--) {
					for (int j = 3; j >= 0; j--) {
						float y = pan[i][j].getY();
						if ((y != panBasePoint[i][j].y)
								&& (y > panBasePoint[i - 1][j].y)) {
							pan[i][j].setY(y - 1);
							selfCheck = true;
						}
					}
				}
				if (!selfCheck)
					check = false;
				break;
			}
			case (TOP_DIRECTION * 10 + 1): {	// 위쪽 돌아가기
				boolean selfCheck = false;
				for (int i = 3; i > 0; i--) {
					for (int j = 3; j >= 0; j--) {
						float y = pan[i][j].getY();
						if (y < panBasePoint[i][j].y) {
							pan[i][j].setY(y + 1);
							selfCheck = true;
						}
					}
				}
				if (!selfCheck)
					check = false;
				break;
			}

			// 게임판 다시 그리기
			case -1:
				if (isBack == 0) {
					if (directionState == RIGHT_DIRECTION)
						rightFunc();
					else if (directionState == LEFT_DIRECTION)
						leftFunc();
					else if (directionState == TOP_DIRECTION)
						topFunc();
					else if (directionState == BOTTOM_DIRECTION)
						bottomFunc();
				}
				for (int i = 0; i < 4; i++)
					for (int j = 0; j < 4; j++) {
						pan[i][j].setX(panBasePoint[i][j].x);
						pan[i][j].setY(panBasePoint[i][j].y);
					}
				viewPan();
				after = false;
				break;

			default:
				break;
			}
		}
	};

	// 패널 이동 애니메이션 쓰레드
	class PanMoveThread extends Thread {

		Handler tHandler;
		int tDirection;
		int tIsBack;

		public PanMoveThread(Handler _Handler, int _Direction, int _isBack) {
			tHandler = _Handler;
			tDirection = _Direction;
			tIsBack = _isBack;
			isBack = _isBack;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			check = true;
			while (check) {
				Message msg = new Message();
				msg.what = tDirection * 10 + tIsBack;
				tHandler.sendMessage(msg);
				try {
					sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Message msg = new Message();
			msg.what = -1;
			tHandler.sendMessage(msg);
		}
	}

	private float getInterval(float p1, float p2) {
		return (p1 - p2);
	}

	// 오른쪽으로 움직였을 시
	void moveRightFunc() {
		for (int i = 0; i < 4; i++) {
			for (int j = 2; j >= 0; j--) {
				if ((digitArray[i][j + 1] == 0)) {
					// 행 전체가 움직이게 한다
					// 움직이는 패널이 모두 0이면 isMove는 false
					for (int m = j; m >= 0; m--) {
						if ((panBasePoint[i][j].x < panPoint[i][j].x + dx)
								&& (panPoint[i][j].x + dx < panBasePoint[i][j + 1].x))
							pan[i][m].setX(panPoint[i][m].x + dx);
						if (digitArray[i][m] != 0)
							isMove = true;
					}
				} else
					switch (digitArray[i][j]) { // 패널 위의 숫자를 판단
					case 0:
						// checkMove = true;
						break;
					case 1:
						for (int m = j; m >= 0; m--)
							if ((digitArray[i][j + 1] == 2)) { // 현재 패널의 오른쪽에 있는
																// 패널의 숫자가 2일때
								if ((panBasePoint[i][j].x < panPoint[i][j].x
										+ dx)
										&& (panPoint[i][j].x + dx < panBasePoint[i][j + 1].x))
									pan[i][m].setX(panPoint[i][m].x + dx);
								isMove = true;
							}
						break;
					case 2:
						for (int m = j; m >= 0; m--)
							if ((digitArray[i][j + 1] == 1)) { // 현재 패널의 오른쪽에 있는
																// 패널의 숫자가 1일때
								if ((panBasePoint[i][j].x < panPoint[i][j].x
										+ dx)
										&& (panPoint[i][j].x + dx < panBasePoint[i][j + 1].x))
									pan[i][m].setX(panPoint[i][m].x + dx);
								isMove = true;
							}
						break;
					default:
						for (int m = j; m >= 0; m--)
							if ((digitArray[i][j + 1] == digitArray[i][j])) { // 현재패널의오른쪽에있는패널의숫자가같을때
								if ((panBasePoint[i][j].x < panPoint[i][j].x
										+ dx)
										&& (panPoint[i][j].x + dx < panBasePoint[i][j + 1].x))
									pan[i][m].setX(panPoint[i][m].x + dx);
								isMove = true;
							}
						break;
					}
			}
		}
	}

	// 아래쪽으로 움직였을 시
	void moveBottomFunc() {
		for (int i = 0; i < 4; i++) {
			for (int j = 2; j >= 0; j--) {
				if ((digitArray[j + 1][i] == 0)) {
					// 행 전체가 움직이게 한다
					for (int m = j; m >= 0; m--) {
						if ((panBasePoint[j][i].y < panPoint[j][i].y + dy)
								&& (panPoint[j][i].y + dy < panBasePoint[j + 1][i].y))
							pan[m][i].setY(panPoint[m][i].y + dy);
						if (digitArray[m][i] != 0)
							isMove = true;
					}
				} else
					switch (digitArray[j][i]) {
					case 0:
						// checkMove = true;
						break;
					case 1:
						for (int m = j; m >= 0; m--)
							if ((digitArray[j + 1][i] == 2)) { // 현재 패널의 아래쪽에 있는
																// 패널의 숫자가 2일때
								if ((panBasePoint[j][i].y < panPoint[j][i].y
										+ dy)
										&& (panPoint[j][i].y + dy < panBasePoint[j + 1][i].y))
									pan[m][i].setY(panPoint[m][i].y + dy);
								isMove = true;
							}
						break;
					case 2:
						for (int m = j; m >= 0; m--)
							if ((digitArray[j + 1][i] == 1)) { // 현재 패널의 아래쪽에 있는
																// 패널의 숫자가 1일때
								if ((panBasePoint[j][i].y < panPoint[j][i].y
										+ dy)
										&& (panPoint[j][i].y + dy < panBasePoint[j + 1][i].y))
									pan[m][i].setY(panPoint[m][i].y + dy);
								isMove = true;
							}
						break;
					default:
						for (int m = j; m >= 0; m--)
							if ((digitArray[j + 1][i] == digitArray[j][i])) { // 현재패널의아래쪽에있는패널의숫자와같을때
								if ((panBasePoint[j][i].y < panPoint[j][i].y
										+ dy)
										&& (panPoint[j][i].y + dy < panBasePoint[j + 1][i].y))
									pan[m][i].setY(panPoint[m][i].y + dy);
								isMove = true;
							}
						break;
					}
			}
		}
	}

	// 왼쪽으로 움직였을 시
	void moveLeftFunc() {
		for (int i = 0; i < 4; i++) {
			for (int j = 1; j <= 3; j++) {
				if ((digitArray[i][j - 1] == 0)) {
					// 행 전체가 움직이게 한다
					for (int m = j; m <= 3; m++) {
						if ((panBasePoint[i][j].x > panPoint[i][j].x + dx)
								&& (panPoint[i][j].x + dx > panBasePoint[i][j - 1].x))
							pan[i][m].setX(panPoint[i][m].x + dx);
						if (digitArray[i][m] != 0)
							isMove = true;
					}
				} else
					switch (digitArray[i][j]) {
					case 0:
						// checkMove = true;
						break;
					case 1:
						for (int m = j; m <= 3; m++)
							if ((digitArray[i][j - 1] == 2)) { // 현재 패널의 왼쪽에 있는
																// 패널의 숫자가 2일때
								if ((panBasePoint[i][j].x > panPoint[i][j].x
										+ dx)
										&& (panPoint[i][j].x + dx > panBasePoint[i][j - 1].x))
									pan[i][m].setX(panPoint[i][m].x + dx);
								isMove = true;
							}
						break;
					case 2:
						for (int m = j; m <= 3; m++)
							if ((digitArray[i][j - 1] == 1)) { // 현재 패널의 오른쪽에 있는
																// 패널의 숫자가 1일때
								if ((panBasePoint[i][j].x > panPoint[i][j].x
										+ dx)
										&& (panPoint[i][j].x + dx > panBasePoint[i][j - 1].x))
									pan[i][m].setX(panPoint[i][m].x + dx);
								isMove = true;
							}
						break;
					default:
						for (int m = j; m <= 3; m++)
							if ((digitArray[i][j - 1] == digitArray[i][j])) { // 현재
																				// 패널의
																				// 왼쪽에
																				// 있는
																				// 패널의
																				// 숫자와
																				// 같을때
								if ((panBasePoint[i][j].x > panPoint[i][j].x
										+ dx)
										&& (panPoint[i][j].x + dx > panBasePoint[i][j - 1].x))
									pan[i][m].setX(panPoint[i][m].x + dx);
								isMove = true;
							}
						break;
					}
			}
		}
	}

	// 위쪽으로 움직였을 시
	void moveTopFunc() {
		for (int i = 0; i < 4; i++) {
			for (int j = 1; j <= 3; j++) {
				if ((digitArray[j - 1][i] == 0)) {
					// 행 전체가 움직이게 한다
					for (int m = j; m <= 3; m++) {
						if ((panBasePoint[j][i].y > panPoint[j][i].y + dy)
								&& (panPoint[j][i].y + dy > panBasePoint[j - 1][i].y))
							pan[m][i].setY(panPoint[m][i].y + dy);
						if (digitArray[m][i] != 0)
							isMove = true;
					}
				} else
					switch (digitArray[j][i]) {
					case 0:
						// checkMove = true;
						break;
					case 1:
						for (int m = j; m <= 3; m++)
							if ((digitArray[j - 1][i] == 2)) { // 현재 패널의 위쪽에 있는
																// 패널의 숫자가 2일때
								if ((panBasePoint[j][i].y > panPoint[j][i].y
										+ dy)
										&& (panPoint[j][i].y + dy > panBasePoint[j - 1][i].y))
									pan[m][i].setY(panPoint[m][i].y + dy);
								isMove = true;
							}
						break;
					case 2:
						for (int m = j; m <= 3; m++)
							if ((digitArray[j - 1][i] == 1)) { // 현재 패널의 위쪽에 있는
																// 패널의 숫자가 1일때
								if ((panBasePoint[j][i].y > panPoint[j][i].y
										+ dy)
										&& (panPoint[j][i].y + dy > panBasePoint[j - 1][i].y))
									pan[m][i].setY(panPoint[m][i].y + dy);
								isMove = true;
							}
						break;
					default:
						for (int m = j; m <= 3; m++)
							if ((digitArray[j - 1][i] == digitArray[j][i])) { // 현재
																				// 패널의
																				// 위쪽에
																				// 있는
																				// 패널의
																				// 숫자와
																				// 같을때
								if ((panBasePoint[j][i].y > panPoint[j][i].y
										+ dy)
										&& (panPoint[j][i].y + dy > panBasePoint[j - 1][i].y))
									pan[m][i].setY(panPoint[m][i].y + dy);
								isMove = true;
							}
						break;
					}
			}
		}
	}

	// 오른쪽으로 swipe했을 시
	void rightFunc() {
		int spaceCount = 0;
		for (int i = 0; i < 4; i++) {
			okNewDigit[i] = false;
			for (int j = 2; j >= 0; j--) {
				if ((digitArray[i][j + 1] == 0)) { // 현재 패널의 오른쪽에 패널이 비어있을 때
					// 해당 줄에서 움직이는 패널이 없을 시 그 줄에서는 새로운 숫자가 등장 안함
					if (digitArray[i][j] != 0) {
						digitArray[i][j + 1] = digitArray[i][j];
						digitArray[i][j] = 0;
						okNewDigit[i] = true;
					}
				} else
					switch (digitArray[i][j]) {
					case 0: // 현재 패널의 오른쪽에 패널이 비어있을 때
						// okNewDigit[i] = true;
						break;
					case 1:
						if ((digitArray[i][j + 1] == 2)) { // 현재 패널의 오른쪽에 패널이 2일
															// 때
							digitArray[i][j + 1] = 3; // 오른쪽으로 두 숫자가 더해져 3이 됨
							digitArray[i][j] = 0;
							okNewDigit[i] = true;
						}
						break;
					case 2:
						if ((digitArray[i][j + 1] == 1)) { // 현재패널의 오른쪽에 패널이 1일
															// 때
							digitArray[i][j + 1] = 3; // 오른쪽으로 두 숫자가 더해져 3이 됨
							digitArray[i][j] = 0;
							okNewDigit[i] = true;
						}
						break;
					default:
						if ((digitArray[i][j + 1] == digitArray[i][j])) { // 현재패널의
																			// 오른쪽에
																			// 패널의
																			// 값이
																			// 같을
																			// 때
							digitArray[i][j + 1] = digitArray[i][j] * 2; // 오른쪽으로
																			// 두
																			// 숫자를
																			// 더함
							digitArray[i][j] = 0;
							okNewDigit[i] = true;
						}
						break;
					}
			}
			if (okNewDigit[i] == true)
				spaceCount++;
		}
		// 다음 숫자 관련
		if (spaceCount == 0) { // 패널이 비어 있는 공간이 없다면 게임종료
			viewDirection.setText("DON'T!");
		} else { // 새로운 패널이 생길 공간이 있다면
			int selectSpace[] = new int[spaceCount];
			int p = 0;
			for (int k = 0; k < 4; k++)
				if (okNewDigit[k] == true)
					selectSpace[p++] = k;
			int selected = (int) (Math.random() * 10 % spaceCount);
			int tmp = selectSpace[selected];
			digitArray[tmp][0] = nextDigit;
		}
		nextDigit = createNextDigit(); // 다음 패널의 값을 결정함
	}

	// 아래쪽으로 swipe했을 시
	void bottomFunc() {
		int spaceCount = 0;
		for (int i = 0; i < 4; i++) {
			okNewDigit[i] = false;
			for (int j = 2; j >= 0; j--) {
				if ((digitArray[j + 1][i] == 0)) {
					if (digitArray[j][i] != 0) {
						digitArray[j + 1][i] = digitArray[j][i];
						digitArray[j][i] = 0;
						okNewDigit[i] = true;
					}
				} else
					switch (digitArray[j][i]) {
					case 0:
						// okNewDigit[i] = true;
						break;
					case 1:
						if ((digitArray[j + 1][i] == 2)) { // 현재 패널의 아래쪽 패널이 2일
															// 때
							digitArray[j + 1][i] = 3;
							digitArray[j][i] = 0;
							okNewDigit[i] = true;
						}
						break;
					case 2:
						if ((digitArray[j + 1][i] == 1)) { // 현재 패널의 아래쪽 패널이 2일
															// 때
							digitArray[j + 1][i] = 3;
							digitArray[j][i] = 0;
							okNewDigit[i] = true;
						}
						break;
					default:
						if ((digitArray[j + 1][i] == digitArray[j][i])) { // 현재
																			// 패널의
																			// 아래쪽
																			// 패널과
																			// 같을
																			// 때
							digitArray[j + 1][i] = digitArray[j][i] * 2;
							digitArray[j][i] = 0;
							okNewDigit[i] = true;
						}
						break;
					}
			}
			if (okNewDigit[i] == true)
				spaceCount++;
		}
		// 다음 숫자 관련
		if (spaceCount == 0) { // 패널이 비어 있는 공간이 없다면 게임종료
			viewDirection.setText("DON'T!");
		} else { // 새로운 패널이 생길 공간이 있다면
			int selectSpace[] = new int[spaceCount];
			int p = 0;
			for (int k = 0; k < 4; k++)
				if (okNewDigit[k] == true)
					selectSpace[p++] = k;
			int selected = (int) (Math.random() * 10 % spaceCount);
			int tmp = selectSpace[selected];
			digitArray[0][tmp] = nextDigit;
		}
		nextDigit = createNextDigit(); // 다음 패널의 값을 결정함
	}

	// 위쪽으로 swipe했을 시
	void topFunc() {
		int spaceCount = 0;
		for (int i = 0; i < 4; i++) {
			okNewDigit[i] = false;
			for (int j = 1; j <= 3; j++) {
				if ((digitArray[j - 1][i] == 0)) {
					if (digitArray[j][i] != 0) {
						digitArray[j - 1][i] = digitArray[j][i];
						digitArray[j][i] = 0;
						okNewDigit[i] = true;
					}
				} else
					switch (digitArray[j][i]) {
					case 0:
						// okNewDigit[i] = true;
						break;
					case 1:
						if ((digitArray[j - 1][i] == 2)) { // 현재 패널의 위쪽 패널이 2일때
							digitArray[j - 1][i] = 3;
							digitArray[j][i] = 0;
							okNewDigit[i] = true;
						}
						break;
					case 2:
						if ((digitArray[j - 1][i] == 1)) { // 현재 패널의 위쪽 패널이 1일때
							digitArray[j - 1][i] = 3;
							digitArray[j][i] = 0;
							okNewDigit[i] = true;
						}
						break;
					default:
						if ((digitArray[j - 1][i] == digitArray[j][i])) { // 현재
																			// 패널의
																			// 위쪽
																			// 패널과
																			// 같을때
							digitArray[j - 1][i] = digitArray[j][i] * 2;
							digitArray[j][i] = 0;
							okNewDigit[i] = true;
						}
						break;
					}
			}
			if (okNewDigit[i] == true)
				spaceCount++;
		}
		// 다음 숫자 관련
		if (spaceCount == 0) { // 패널이 비어 있는 공간이 없다면 게임종료
			viewDirection.setText("DON'T!");
		} else { // 새로운 패널이 생길 공간이 있다면
			int selectSpace[] = new int[spaceCount];
			int p = 0;
			for (int k = 0; k < 4; k++)
				if (okNewDigit[k] == true)
					selectSpace[p++] = k;
			int selected = (int) (Math.random() * 10 % spaceCount);
			int tmp = selectSpace[selected];
			digitArray[3][tmp] = nextDigit;
		}
		nextDigit = createNextDigit(); // 다음 패널의 값을 결정함
	}

	// 왼쪽으로 swipe했을 시
	void leftFunc() {
		int spaceCount = 0;
		for (int i = 0; i < 4; i++) {
			okNewDigit[i] = false;
			for (int j = 1; j <= 3; j++) {
				if ((digitArray[i][j - 1] == 0)) {
					if (digitArray[i][j] != 0) {
						digitArray[i][j - 1] = digitArray[i][j];
						digitArray[i][j] = 0;
						okNewDigit[i] = true;
					}
				} else
					switch (digitArray[i][j]) {
					case 0:
						// okNewDigit[i] = true;
						break;
					case 1:
						if ((digitArray[i][j - 1] == 2)) { // 현재 패널의 왼쪽 패널이 2일때
							digitArray[i][j - 1] = 3;
							digitArray[i][j] = 0;
							okNewDigit[i] = true;
						}
						break;
					case 2:
						if ((digitArray[i][j - 1] == 1)) { // 현재 패널의 왼쪽 패널이 1일때
							digitArray[i][j - 1] = 3;
							digitArray[i][j] = 0;
							okNewDigit[i] = true;
						}
						break;
					default:
						if ((digitArray[i][j - 1] == digitArray[i][j])) { // 현재
																			// 패널의
																			// 왼쪽
																			// 패널과
																			// 같을때
							digitArray[i][j - 1] = digitArray[i][j] * 2;
							digitArray[i][j] = 0;
							okNewDigit[i] = true;
						}
						break;
					}
			}
			if (okNewDigit[i] == true)
				spaceCount++;
		}
		// 다음 숫자 관련
		if (spaceCount == 0) { // 패널이 비어 있는 공간이 없다면 게임종료
			viewDirection.setText("DON'T!");
		} else { // 새로운 패널이 생길 공간이 있다면
			int selectSpace[] = new int[spaceCount];
			int p = 0;
			for (int k = 0; k < 4; k++)
				if (okNewDigit[k] == true)
					selectSpace[p++] = k;
			int selected = (int) (Math.random() * 10 % spaceCount);
			int tmp = selectSpace[selected];
			digitArray[tmp][3] = nextDigit;
		}
		nextDigit = createNextDigit(); // 다음 패널의 값을 결정함
	}

	// view
	void viewPan() {
		String tempDA = new String();
		digitManager[0] = 0;
		digitManager[1] = 0;
		// 숫자 변경, 배경색 변경
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				// digitArray[i][j] = (int) (Math.random()*10 % 4);
				// !임시 확인용
				tempDA += digitArray[i][j] + " ";
				if (digitArray[i][j] == 0) {
					pan[i][j].setBackgroundColor(Color.TRANSPARENT);
					pan[i][j].setText("");
				} else if (digitArray[i][j] == 1) {
					digitManager[0]++;
					pan[i][j].setBackgroundColor(Color.parseColor("#ffff8800"));
					pan[i][j].setText("" + digitArray[i][j]);
				} else if (digitArray[i][j] == 2) {
					digitManager[1]++;
					pan[i][j].setBackgroundColor(Color.parseColor("#ff669900"));
					pan[i][j].setText("" + digitArray[i][j]);
				} else {
					if (highDigit < digitArray[i][j])
						highDigit = digitArray[i][j];
					pan[i][j].setBackgroundColor(Color.parseColor("#ff0099cc"));
					pan[i][j].setText("" + digitArray[i][j]);
				}
			}
			// !임시 확인용
			tempDA += "\n";
		}
		// 가장 높은 수 글자색 설정
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (highDigit == digitArray[i][j])
					pan[i][j].setTextColor(Color.parseColor("#ffffbb33"));
				else
					pan[i][j].setTextColor(Color.WHITE);
			}
		}

		// !임시 확인용
		// viewDigitState.setText(tempDA);
	}

	// 다음 숫자 생성 알고리즘
	int createNextDigit() {

		int tmp = nextDigit;

		if (tmp == 0)
			tmp = (int) (Math.random() * 10 % 4);
		else {
			// 시드 설정 후 난수 발생
			Random rm = new Random();
			rm.setSeed(System.currentTimeMillis());
			int per = rm.nextInt(100);

			switch (tmp) {
			case 1:
				if (per < 60)
					tmp = 2;
				else if (per > 70)
					tmp = 3;
				else
					tmp = 1;
				break;
			case 2:
				if (per < 60)
					tmp = 1;
				else if (per > 70)
					tmp = 3;
				else
					tmp = 2;
				break;
			default:
				if (per < 80) {
					if (digitManager[0] < digitManager[1])
						tmp = 1;
					else
						tmp = 2;
				} else
					tmp = 3;
				break;
			}
		}

		if (tmp == 1)
			viewDigitState.setBackgroundColor(Color.parseColor("#ffff8800"));
		else if (tmp == 2)
			viewDigitState.setBackgroundColor(Color.parseColor("#ff669900"));
		else
			viewDigitState.setBackgroundColor(Color.parseColor("#ff0099cc"));
		viewNextDigit.setText("Next Digit: " + tmp);

		return tmp;
	}

}