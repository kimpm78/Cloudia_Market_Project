const CM_06_1000_stepIndicator = ({ currentStep, status }) => {
  const steps = ['商品情報', '注文・決済', '注文完了'];

  let finalStepLabel = '注文完了';

  if (status === 'fail') {
    finalStepLabel = '決済失敗';
  } else if (status === 'pending') {
    finalStepLabel = '入金待ち';
  }

  const renderedSteps = [...steps];
  renderedSteps[2] = finalStepLabel;

  return (
    <div className="d-flex gap-2">
      {renderedSteps.map((step, idx) => (
        <span key={step} className={idx === currentStep ? 'fw-bold text-primary' : 'text-muted'}>
          {step}
          {idx < renderedSteps.length - 1 && <i className="bi bi-caret-right-fill mx-1"></i>}
        </span>
      ))}
    </div>
  );
};

export default CM_06_1000_stepIndicator;